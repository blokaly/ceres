package com.blokaly.ceres.bitfinex.callback;

import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.bitfinex.event.EventType;
import com.blokaly.ceres.bitfinex.event.PingEvent;
import com.blokaly.ceres.bitfinex.event.PongEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PingPongHandler implements CommandCallbackHandler<AbstractEvent> {
  @Override
  public AbstractEvent handleEvent(JsonElement json, JsonDeserializationContext context) {
    JsonObject evt = json.getAsJsonObject();
    EventType eventType = EventType.get(evt.get("event").getAsString());
    if (eventType == EventType.PING) {
      return context.deserialize(json, PingEvent.class);
    } else if (eventType == EventType.PONG) {
      return context.deserialize(json, PongEvent.class);
    } else {
      return null;
    }
  }
}
