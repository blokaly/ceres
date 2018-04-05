package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.callback.CommandCallbackHandler;
import com.blokaly.ceres.anx.event.AbstractEvent;
import com.blokaly.ceres.anx.event.EventType;
import com.blokaly.ceres.anx.event.NoOpEvent;
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
    String eventType = jsonObject.get("event").getAsString();
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
