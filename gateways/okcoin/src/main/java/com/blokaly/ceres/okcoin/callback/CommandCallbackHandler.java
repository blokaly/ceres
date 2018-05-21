package com.blokaly.ceres.okcoin.callback;

import com.blokaly.ceres.okcoin.event.ChannelEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public interface CommandCallbackHandler<T extends ChannelEvent>{
  T parseJson(JsonElement json, JsonDeserializationContext context);
}

