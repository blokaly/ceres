package com.blokaly.ceres.gdax.callback;

import com.blokaly.ceres.gdax.event.OpenEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class OpenCallbackHandler implements CommandCallbackHandler<OpenEvent> {


  @Override
  public OpenEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, OpenEvent.class);
  }
}
