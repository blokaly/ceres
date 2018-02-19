package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.callback.CommandCallbackHandler;
import com.blokaly.ceres.bitfinex.event.*;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class JsonCracker {

    private static Logger LOGGER = LoggerFactory.getLogger(JsonCracker.class);
    private final Gson gson;
    private final WebSocket ws;

    private OrderBasedOrderBook orderBook = new OrderBasedOrderBook("BTCUSD");

    @Inject
    public JsonCracker(WebSocket ws, Map<String, CommandCallbackHandler> handlers) {
        this.ws = ws;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AbstractEvent.class, new EventAdapter(handlers));
        gson = builder.create();
    }

    public void crack(String json) {
        AbstractEvent event = gson.fromJson(json, AbstractEvent.class);
        LOGGER.info("event: {}", event);

        if (event instanceof InfoEvent) {
            String jsonString = gson.toJson(new OrderBookEvent("BTCUSD"));
            LOGGER.info("subscribe: {}", jsonString);
            ws.send(jsonString);
        } else if (event instanceof SnapshotEvent) {
            orderBook.processSnapshot((SnapshotEvent)event);
        } else if (event instanceof RefreshEvent) {
            orderBook.processIncrementalUpdate((RefreshEvent)event);
        }

        LOGGER.info("ob: {}", orderBook.tob());
    }
}
