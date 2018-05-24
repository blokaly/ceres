package com.blokaly.ceres.huobi;


import com.blokaly.ceres.huobi.callback.CommandCallbackHandler;
import com.blokaly.ceres.huobi.event.EventType;
import com.blokaly.ceres.huobi.event.NoOpEvent;
import com.blokaly.ceres.huobi.event.WSEvent;
import com.google.gson.*;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

import static com.blokaly.ceres.huobi.event.EventType.*;

public class EventAdapter implements JsonDeserializer<WSEvent> {

  private static Logger LOGGER = LoggerFactory.getLogger(EventAdapter.class);
  private final Map<EventType, CommandCallbackHandler> handlers;

  private final NoOpEvent noOpEvent = new NoOpEvent();
  @Inject
  public EventAdapter(Map<EventType, CommandCallbackHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public WSEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    JsonObject jsonObject = json.getAsJsonObject();
    EventType type = null;
    if (jsonObject.has(TICK.getType())) {
      type = TICK;
    } else if (jsonObject.has(PING.getType())) {
      type = PING;
    } else if (jsonObject.has(SUBSCRIBED.getType())) {
      type = SUBSCRIBED;
    } else if (jsonObject.has(PONG.getType())) {
      type = PONG;
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
