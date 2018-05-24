package com.blokaly.ceres.cex.callback;

import com.blokaly.ceres.cex.event.EventType;
import com.blokaly.ceres.cex.event.MDUpdateEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MDUpdateCallbackHandler implements CommandCallbackHandler<MDUpdateEvent> {
  @Override
  public MDUpdateEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    JsonObject jsonObject = json.getAsJsonObject();

    String type = jsonObject.get("e").getAsString();
    if (!EventType.UPDATE.getType().equals(type)) {
      throw new IllegalArgumentException("message is not type of " + EventType.UPDATE.getType() + ": " + json);
    }

    JsonObject data = jsonObject.getAsJsonObject("data");
    String pair = data.get("pair").getAsString();
    long id = data.get("id").getAsLong();
    JsonArray bids = data.get("bids").getAsJsonArray();
    JsonArray asks = data.get("asks").getAsJsonArray();
    return MDUpdateEvent.parse(id, pair, bids, asks);
  }
}
