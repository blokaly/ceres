package com.blokaly.ceres.huobi.callback;

import com.blokaly.ceres.huobi.event.SnapshotEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SnapshotCallbackHandler implements CommandCallbackHandler<SnapshotEvent> {

  @Override
  public SnapshotEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    JsonObject jsonObject = json.getAsJsonObject();
    String channel = jsonObject.get("ch").getAsString();
    String symbol = channel.substring(7, channel.indexOf('.', 7));
    JsonObject tick = jsonObject.getAsJsonObject("tick");
    long ts = tick.get("ts").getAsLong();
    JsonArray bids = tick.get("bids").getAsJsonArray();
    JsonArray asks = tick.get("asks").getAsJsonArray();
    return SnapshotEvent.parse(ts, symbol, bids, asks);
  }
}
