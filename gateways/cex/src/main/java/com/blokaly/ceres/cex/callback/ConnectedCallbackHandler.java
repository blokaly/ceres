package com.blokaly.ceres.cex.callback;

import com.blokaly.ceres.cex.event.ConnectedEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class ConnectedCallbackHandler implements CommandCallbackHandler<ConnectedEvent> {

  @Override
  public ConnectedEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, ConnectedEvent.class);
  }
}