package com.blokaly.ceres.huobi.callback;

import com.blokaly.ceres.huobi.event.SubbedEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class SubscribedCallbackHandler implements CommandCallbackHandler<SubbedEvent> {

  @Override
  public SubbedEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, SubbedEvent.class);
  }
}