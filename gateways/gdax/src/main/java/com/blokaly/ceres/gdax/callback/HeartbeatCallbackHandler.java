package com.blokaly.ceres.gdax.callback;

import com.blokaly.ceres.gdax.event.HbEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class HeartbeatCallbackHandler implements CommandCallbackHandler<HbEvent> {

  @Override
  public HbEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, HbEvent.class);
  }
}
