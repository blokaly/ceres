package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MessageHandlerImpl implements MessageHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private final Gson gson;
    private final MessageSender sender;
    private final OrderBookKeeper bookKeeper;
    private final ToBProducer producer;

    public MessageHandlerImpl(Gson gson, MessageSender sender, OrderBookKeeper bookKeeper, ToBProducer producer) {
        this.gson = gson;
        this.sender = sender;
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
    public void onMessage(InfoEvent event) {
        String version = event.getVersion();
        if (version != null) {
            String[] vers = version.split("\\.");
            if (!"1".equals(vers[0])) {
                LOGGER.error("Unsupported version: {}, only v1 supported.", version);
                return;
            }
            bookKeeper.getSymbols().forEach(symbol -> {
                String jsonString = gson.toJson(new OrderBookEvent(symbol));
                LOGGER.info("subscribe: {}", jsonString);
                sender.send(jsonString);
            });
        } else {
            LOGGER.info("Received info {}", event);
        }

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
