package com.blokaly.ceres.gdax.callback;

import com.blokaly.ceres.gdax.event.L2UpdateEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RefreshCallbackHandler implements CommandCallbackHandler<L2UpdateEvent>{

  @Override
  public L2UpdateEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    JsonObject update = json.getAsJsonObject();
    String productId = update.get("product_id").getAsString();
    JsonArray changes = update.getAsJsonArray("changes");
    return L2UpdateEvent.parse(System.nanoTime(), productId, changes);
  }
}
