package com.blokaly.ceres.gdax.callback;

import com.blokaly.ceres.gdax.event.AbstractEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public interface CommandCallbackHandler <T extends AbstractEvent>{
  T parseJson(JsonElement json, JsonDeserializationContext context);
}

