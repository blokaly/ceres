package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;
import com.blokaly.ceres.common.SingleThread;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

@Singleton
public class JsonCracker {

    private static Logger LOGGER = LoggerFactory.getLogger(JsonCracker.class);
    private final Gson gson;

    private final MessageHandler messageHandler;
    private final ExecutorService executor;

    @Inject
    public JsonCracker(Gson gson, MessageHandler messageHandler, @SingleThread ExecutorService executor) {
        this.gson = gson;
        this.messageHandler = messageHandler;
        this.executor = executor;
    }

    public void crack(String json) {
        AbstractEvent event = gson.fromJson(json, AbstractEvent.class);
        LOGGER.debug("event: {}", event);
        executor.execute(() -> process(event));
    }

    private void process(AbstractEvent event) {
        EventType type = EventType.get(event.getEvent());
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                case HB:
                    messageHandler.onMessage((HbEvent) event);
                    break;
                case PING:
                    messageHandler.onMessage((PingEvent) event);
                    break;
                case PONG:
                    messageHandler.onMessage((PongEvent) event);
                    break;
                case INFO:
                    messageHandler.onMessage((InfoEvent) event);
                    break;
                case SUBSCRIBED:
                    messageHandler.onMessage((SubscribedEvent) event);
                    break;
                case SNAPSHOT:
                    messageHandler.onMessage((SnapshotEvent) event);
                    break;
                case REFRESH:
                    messageHandler.onMessage((RefreshEvent) event);
                    break;
                case ERROR:
                    messageHandler.onMessage((ErrorEvent) event);
            }
        } catch (Exception ex) {
            LOGGER.error("Error cracking event", ex);
        }
    }
}
