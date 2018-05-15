package com.blokaly.ceres.cex.callback;

import com.blokaly.ceres.cex.event.AbstractEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public interface CommandCallbackHandler<T extends AbstractEvent>{
  T parseJson(JsonElement json, JsonDeserializationContext context);
}

