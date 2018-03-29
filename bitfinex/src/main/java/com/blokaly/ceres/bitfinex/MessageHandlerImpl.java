package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageHandlerImpl implements MessageHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private final Gson gson;
    private final Provider<BitfinexClient> clientProvider;
    private final OrderBookKeeper bookKeeper;
    private final ToBProducer producer;

    @Inject
    public MessageHandlerImpl(Gson gson, Provider<BitfinexClient> clientProvider, OrderBookKeeper bookKeeper, ToBProducer producer) {
        this.gson = gson;
        this.clientProvider = clientProvider;
        this.bookKeeper = bookKeeper;
        this.producer = producer;
    }

    @Override
    public void onMessage(HbEvent event) {
        LOGGER.debug("HB[{}]", bookKeeper.getSymbol(event.getChannelId()));
    }

    @Override
    public void onMessage(PingEvent event) {
        LOGGER.info("PING");
    }

    @Override
    public void onMessage(PongEvent event) {
        LOGGER.info("PONG");
    }

    @Override
    public void onMessage(ErrorEvent event) {
        LOGGER.error("{}", event);
    }

    @Override
    public void onMessage(InfoEvent event) {
        String version = event.getVersion();
        if (version != null) {
            String[] vers = version.split("\\.");
            if (!"1".equals(vers[0])) {
                LOGGER.error("Unsupported version: {}, only v1 supported.", version);
                return;
            }
            subscribeAll();
        } else {
            LOGGER.info("Received info {}", event);
            InfoEvent.Status status = event.getStatus();
            if (status == InfoEvent.Status.WEB_SOCKET_RESTART || status == InfoEvent.Status.PAUSE) {
                bookKeeper.getAllBooks().forEach(book -> {
                    book.clear();
                    producer.publish(book);
                });
            } else if (status == InfoEvent.Status.RESUME) {
                //TODO unsub
                subscribeAll();
            }
        }

    }

    private void subscribeAll() {
        BitfinexClient sender = clientProvider.get();
        if (sender == null) {
            LOGGER.error("Bitfinex client unavailable, skip subscription");
            return;
        }
        bookKeeper.getSymbols().forEach(symbol -> {
            String jsonString = gson.toJson(new OrderBookEvent(symbol));
            LOGGER.info("subscribe: {}", jsonString);
            sender.send(jsonString);
        });
    }

    @Override
    public void onMessage(SubscribedEvent event) {
        int chanId = event.getChanId();
        String symbol = event.getPair();
        bookKeeper.makeOrderBook(chanId, symbol);
    }

    @Override
    public void onMessage(SnapshotEvent event) {
        OrderBasedOrderBook orderBook = bookKeeper.get(event.getChannelId());
        if (orderBook == null) {
            throw new IllegalStateException("No order book for channel id " + event.getChannelId());
        }
        orderBook.processSnapshot(event);
    }

    @Override
    public void onMessage(RefreshEvent event) {
        OrderBasedOrderBook orderBook = bookKeeper.get(event.getChannelId());
        if (orderBook == null) {
            throw new IllegalStateException("No order book for channel id " + event.getChannelId());
        }
        orderBook.processIncrementalUpdate(event);
        producer.publish(orderBook);
    }
}
