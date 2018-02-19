package com.blokaly.ceres.bitfinex.callback;

import com.blokaly.ceres.bitfinex.event.SubscribedEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class SubscribedCallbackHandler implements CommandCallbackHandler<SubscribedEvent> {
    @Override
    public SubscribedEvent handleChannelData(JsonElement json, JsonDeserializationContext context) {
        return context.deserialize(json, SubscribedEvent.class);
    }
}
