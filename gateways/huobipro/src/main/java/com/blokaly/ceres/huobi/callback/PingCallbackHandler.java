package com.blokaly.ceres.huobi.callback;

import com.blokaly.ceres.huobi.event.PingEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class PingCallbackHandler implements CommandCallbackHandler<PingEvent> {

  @Override
  public PingEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, PingEvent.class);
  }
}
