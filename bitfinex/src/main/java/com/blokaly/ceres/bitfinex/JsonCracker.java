package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.bitfinex.event.InfoEvent;
import com.blokaly.ceres.bitfinex.event.RefreshEvent;
import com.blokaly.ceres.bitfinex.event.SnapshotEvent;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JsonCracker {

    private static Logger LOGGER = LoggerFactory.getLogger(JsonCracker.class);
    private final Gson gson;

    private final OrderBasedOrderBook orderBook = new OrderBasedOrderBook("BTCUSD");
    private final Provider<MessageHandler> messageHandlerProvider;

    @Inject
    public JsonCracker(Gson gson, Provider<MessageHandler> messageHandlerProvider) {
        this.gson = gson;
        this.messageHandlerProvider = messageHandlerProvider;
    }

    public void crack(String json) {
        AbstractEvent event = gson.fromJson(json, AbstractEvent.class);
        LOGGER.info("event: {}", event);

        if (event instanceof InfoEvent) {
            messageHandlerProvider.get().onMessage((InfoEvent)event);
        } else if (event instanceof SnapshotEvent) {
            orderBook.processSnapshot((SnapshotEvent)event);
        } else if (event instanceof RefreshEvent) {
            orderBook.processIncrementalUpdate((RefreshEvent)event);
        }

        LOGGER.info("ob: {}", orderBook.tob());
    }
}
