package com.blokaly.ceres.cex.callback;

import com.blokaly.ceres.cex.event.PingEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class PingCallbackHandler implements CommandCallbackHandler<PingEvent> {
  @Override
  public PingEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, PingEvent.class);
  }
}
