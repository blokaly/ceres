package com.blokaly.ceres.huobi.callback;

import com.blokaly.ceres.huobi.event.PongEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class PongCallbackHandler implements CommandCallbackHandler<PongEvent> {

  @Override
  public PongEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, PongEvent.class);
  }
}
