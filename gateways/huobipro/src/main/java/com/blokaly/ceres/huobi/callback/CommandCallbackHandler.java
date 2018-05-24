package com.blokaly.ceres.huobi.callback;

import com.blokaly.ceres.huobi.event.WSEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public interface CommandCallbackHandler<T extends WSEvent>{
  T parseJson(JsonElement json, JsonDeserializationContext context);
}

