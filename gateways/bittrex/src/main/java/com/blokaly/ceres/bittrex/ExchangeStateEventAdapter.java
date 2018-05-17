package com.blokaly.ceres.bittrex;

import com.blokaly.ceres.bittrex.event.ExchangeStateEvent;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

import static com.blokaly.ceres.bittrex.JsonKey.Nonce;

public class ExchangeStateEventAdapter implements JsonDeserializer<ExchangeStateEvent> {

  private static Logger LOGGER = LoggerFactory.getLogger(ExchangeStateEventAdapter.class);

  @Override
  public ExchangeStateEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    JsonObject jsonObject = json.getAsJsonObject();
    long sequence = jsonObject.get(Nonce.getKey()).getAsLong();
    JsonArray bids = jsonObject.getAsJsonArray(JsonKey.Buys.getKey());
    JsonArray asks = jsonObject.getAsJsonArray(JsonKey.Sells.getKey());
    return ExchangeStateEvent.parse(sequence, bids, asks);
  }
}