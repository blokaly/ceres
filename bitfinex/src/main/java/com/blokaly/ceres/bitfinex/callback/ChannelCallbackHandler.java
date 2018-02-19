package com.blokaly.ceres.bitfinex.callback;

import com.blokaly.ceres.bitfinex.event.ChannelEvent;
import com.blokaly.ceres.bitfinex.event.HbEvent;
import com.blokaly.ceres.bitfinex.event.RefreshEvent;
import com.blokaly.ceres.bitfinex.event.SnapshotEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class ChannelCallbackHandler implements CommandCallbackHandler<ChannelEvent>{

    private static long sequence = 0;

    @Override
    public ChannelEvent handleChannelData(JsonElement json, JsonDeserializationContext context) {
        JsonArray data = json.getAsJsonArray();
        int channelId = data.get(0).getAsInt();
        JsonElement element = data.get(1);
        if (element.isJsonArray()) {
            return SnapshotEvent.parse(channelId, ++sequence, element.getAsJsonArray());
        } else {
            String stringData = element.getAsString();
            if ("hb".equals(stringData)) {
                return new HbEvent(channelId);
            } else {
                return new RefreshEvent(channelId, ++sequence, data);
            }
        }
    }
}
