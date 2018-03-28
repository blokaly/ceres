package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;
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

    private final Provider<MessageHandler> messageHandlerProvider;

    @Inject
    public JsonCracker(Gson gson, Provider<MessageHandler> messageHandlerProvider) {
        this.gson = gson;
        this.messageHandlerProvider = messageHandlerProvider;
    }

    public void crack(String json) {
        AbstractEvent event = gson.fromJson(json, AbstractEvent.class);
        LOGGER.info("event: {}", event);
        EventType type = EventType.get(event.getEvent());
        if (type == null) {
            return;
        }
        switch (type) {
            case INFO:
                messageHandlerProvider.get().onMessage((InfoEvent)event);
                break;
            case SUBSCRIBED:
                messageHandlerProvider.get().onMessage((SubscribedEvent)event);
                break;
            case SNAPSHOT:
                messageHandlerProvider.get().onMessage((SnapshotEvent)event);
                break;
            case REFRESH:
                messageHandlerProvider.get().onMessage((RefreshEvent)event);
                break;

        }
    }
}
