package com.blokaly.ceres.bitfinex.callback;

import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public interface CommandCallbackHandler <T extends AbstractEvent>{
    T handleEvent(JsonElement json, JsonDeserializationContext context);
}
