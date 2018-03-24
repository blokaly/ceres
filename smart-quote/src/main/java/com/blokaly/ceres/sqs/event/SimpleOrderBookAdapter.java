package com.blokaly.ceres.sqs.event;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class SimpleOrderBookAdapter implements JsonDeserializer<SimpleOrderBook>{

  private static Logger LOGGER = LoggerFactory.getLogger(SimpleOrderBookAdapter.class);

  @Override
  public SimpleOrderBook deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    JsonArray array = json.getAsJsonArray();
    JsonArray bids = array.get(0).getAsJsonArray();
    JsonArray asks = array.get(1).getAsJsonArray();
    return SimpleOrderBook.parse(bids, asks);
  }
}
