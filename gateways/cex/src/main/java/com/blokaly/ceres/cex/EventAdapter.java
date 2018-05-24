package com.blokaly.ceres.cex;


import com.blokaly.ceres.cex.callback.CommandCallbackHandler;
import com.blokaly.ceres.cex.event.AbstractEvent;
import com.blokaly.ceres.cex.event.EventType;
import com.blokaly.ceres.cex.event.NoOpEvent;
import com.google.gson.*;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public class EventAdapter implements JsonDeserializer<AbstractEvent> {

  private static Logger LOGGER = LoggerFactory.getLogger(EventAdapter.class);
  private final Map<EventType, CommandCallbackHandler> handlers;

  private final NoOpEvent noOpEvent = new NoOpEvent();
  @Inject
  public EventAdapter(Map<EventType, CommandCallbackHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public AbstractEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    EventType type = null;
    JsonObject jsonObject = json.getAsJsonObject();
    if (jsonObject.has("e")) {
      type = EventType.get(jsonObject.get("e").getAsString());
    }

    if (type == null) {
      LOGGER.error("unknown event: {}", jsonObject);
      return noOpEvent;
    }

    CommandCallbackHandler handler = handlers.get(type);
    if (handler == null) {
      LOGGER.error("No handler for event: {}", jsonObject);
      return noOpEvent;
    } else {
      return handler.parseJson(json, context);
    }
  }
}
