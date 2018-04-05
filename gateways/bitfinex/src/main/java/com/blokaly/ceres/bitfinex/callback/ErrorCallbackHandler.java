package com.blokaly.ceres.bitfinex.callback;

import com.blokaly.ceres.bitfinex.event.ErrorEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class ErrorCallbackHandler implements CommandCallbackHandler<ErrorEvent> {

  @Override
  public ErrorEvent handleEvent(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, ErrorEvent.class);
  }
}
