package com.blokaly.ceres.gdax.callback;

import com.blokaly.ceres.gdax.event.SubscribedEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class SubscribedCallbackHandler implements CommandCallbackHandler<SubscribedEvent> {

  @Override
  public SubscribedEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, SubscribedEvent.class);
  }
}
