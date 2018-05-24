package com.blokaly.ceres.cex.callback;

import com.blokaly.ceres.cex.event.EventType;
import com.blokaly.ceres.cex.event.SnapshotEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SnapshotCallbackHandler implements CommandCallbackHandler<SnapshotEvent> {

  @Override
  public SnapshotEvent parseJson(JsonElement json, JsonDeserializationContext context) {

    JsonObject jsonObject = json.getAsJsonObject();

    String type = jsonObject.get("e").getAsString();
    if (!EventType.SUBSCRIBE.getType().equals(type)) {
      throw new IllegalArgumentException("message is not type of " + EventType.SUBSCRIBE.getType() + ": " + json);
    }

    String ok = jsonObject.get("ok").getAsString();
    JsonObject data = jsonObject.getAsJsonObject("data");
    String pair = data.get("pair").getAsString();
    if ("ok".equals(ok)) {
    long id = data.get("id").getAsLong();
    JsonArray bids = data.get("bids").getAsJsonArray();
    JsonArray asks = data.get("asks").getAsJsonArray();
      return SnapshotEvent.parse(id, pair, bids, asks);
    } else {
      String error = null;
      if (data.has("error")) {
        error = data.get("error").getAsString();
      }
      return SnapshotEvent.fail(pair, System.currentTimeMillis(), error);
    }
  }
}