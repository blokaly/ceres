package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;
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
    private final BitfinexKafkaProducer producer;

    public MessageHandlerImpl(Gson gson, MessageSender sender, OrderBookKeeper bookKeeper, BitfinexKafkaProducer producer) {
        this.gson = gson;
        this.sender = sender;
        this.bookKeeper = bookKeeper;
        this.producer = producer;
    }

    @Override
    public void onMessage(InfoEvent event) {
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
