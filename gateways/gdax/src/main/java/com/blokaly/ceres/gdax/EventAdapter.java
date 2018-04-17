package com.blokaly.ceres.gdax;

import com.blokaly.ceres.gdax.callback.CommandCallbackHandler;
import com.blokaly.ceres.gdax.event.AbstractEvent;
import com.blokaly.ceres.gdax.event.EventType;
import com.blokaly.ceres.gdax.event.NoOpEvent;
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

    JsonObject jsonObject = json.getAsJsonObject();
    String eventType = jsonObject.get("type").getAsString();
    EventType type = EventType.get(eventType);
    if (type == null) {
      LOGGER.error("unknown event type: {}", eventType);
      return noOpEvent;
    }
    CommandCallbackHandler handler = handlers.get(type);
    if (handler == null) {
      LOGGER.error("unknown event: {}", json);
      return noOpEvent;
    } else {
      return handler.parseJson(json, context);
    }
  }
}
