package com.blokaly.ceres.gdax.callback;

import com.blokaly.ceres.gdax.event.SnapshotEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SnapshotCallbackHandler implements CommandCallbackHandler<SnapshotEvent> {

  @Override
  public SnapshotEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    JsonObject jsonObject = json.getAsJsonObject();
    String productId = jsonObject.get("product_id").getAsString();
    JsonArray bids = jsonObject.get("bids").getAsJsonArray();
    JsonArray asks = jsonObject.get("asks").getAsJsonArray();
    return SnapshotEvent.parse(System.currentTimeMillis(), productId, bids, asks);
  }
}
