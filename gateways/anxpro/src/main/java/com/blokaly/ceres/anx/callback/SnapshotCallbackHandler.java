package com.blokaly.ceres.anx.callback;

import com.blokaly.ceres.anx.event.SnapshotEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SnapshotCallbackHandler implements CommandCallbackHandler<SnapshotEvent> {

  @Override
  public SnapshotEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    JsonObject jsonObject = json.getAsJsonObject();
    JsonObject data = jsonObject.getAsJsonArray("data").get(0).getAsJsonObject();
    long millis = data.get("timestampMillis").getAsLong();
    String productId = data.get("ccyPair").getAsString();
    JsonArray bids = data.get("bids").getAsJsonArray();
    JsonArray asks = data.get("asks").getAsJsonArray();
    return SnapshotEvent.parse(millis, productId, bids, asks);
  }
}
