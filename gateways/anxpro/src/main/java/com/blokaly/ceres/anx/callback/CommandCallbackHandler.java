package com.blokaly.ceres.anx.callback;

import com.blokaly.ceres.anx.event.AbstractEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public interface CommandCallbackHandler<T extends AbstractEvent>{
  T parseJson(JsonElement json, JsonDeserializationContext context);
}

