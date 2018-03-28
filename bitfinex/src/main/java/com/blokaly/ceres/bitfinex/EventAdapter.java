package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.callback.CommandCallbackHandler;
import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.bitfinex.event.EventType;
import com.blokaly.ceres.bitfinex.event.NoOpEvent;
import com.google.gson.*;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public class EventAdapter implements JsonDeserializer<AbstractEvent>{

    private static Logger LOGGER = LoggerFactory.getLogger(EventAdapter.class);
    private final Map<EventType, CommandCallbackHandler> handlers;

    private final NoOpEvent noOpEvent = new NoOpEvent();
    @Inject
    public EventAdapter(Map<EventType, CommandCallbackHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public AbstractEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        EventType eventType = EventType.CHANNEL;
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            eventType = EventType.get(jsonObject.get("event").getAsString());
        } else if (json.isJsonArray()) {
            int channelId = json.getAsJsonArray().get(0).getAsInt();
            if (channelId <= 0) {
                eventType = null;
            }
        }

        if (eventType == null) {
            LOGGER.error("unknown event: {}", json);
            return noOpEvent;
        }

        CommandCallbackHandler handler = handlers.get(eventType);
        if (handler == null) {
            LOGGER.error("unknown event: {}", json);
            return noOpEvent;
        } else {
            return handler.handleChannelData(json, context);
        }


    }
}
