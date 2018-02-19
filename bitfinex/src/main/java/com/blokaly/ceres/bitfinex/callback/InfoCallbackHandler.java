package com.blokaly.ceres.bitfinex.callback;

import com.blokaly.ceres.bitfinex.event.InfoEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class InfoCallbackHandler implements CommandCallbackHandler<InfoEvent> {


    @Override
    public InfoEvent handleChannelData(JsonElement json, JsonDeserializationContext context) {
        return context.deserialize(json, InfoEvent.class);
    }
}
