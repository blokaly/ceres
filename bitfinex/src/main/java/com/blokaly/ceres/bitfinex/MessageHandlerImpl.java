package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.blokaly.ceres.orderbook.OrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageHandlerImpl implements MessageHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private final Gson gson;
    private final MessageSender sender;
    private final OrderBookKeeper bookKeeper;

    @Inject
    public MessageHandlerImpl(Gson gson, MessageSender sender, OrderBookKeeper bookKeeper) {
        this.gson = gson;
        this.sender = sender;
        this.bookKeeper = bookKeeper;
    }

    @Override
    public void onMessage(InfoEvent event) {
        String jsonString = gson.toJson(new OrderBookEvent("BTCUSD"));
        LOGGER.info("subscribe: {}", jsonString);
        sender.send(jsonString);
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
    }
}
