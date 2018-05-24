package com.blokaly.ceres.bittrex;

import com.blokaly.ceres.bittrex.event.ExchangeDeltaEvent;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

import static com.blokaly.ceres.bittrex.JsonKey.MarketName;
import static com.blokaly.ceres.bittrex.JsonKey.Nonce;

public class ExchangeDeltaEventAdapter implements JsonDeserializer<ExchangeDeltaEvent> {

  private static Logger LOGGER = LoggerFactory.getLogger(ExchangeDeltaEventAdapter.class);

  @Override
  public ExchangeDeltaEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    JsonObject jsonObject = json.getAsJsonObject();
    String market = jsonObject.get(MarketName.getKey()).getAsString();
    long sequence = jsonObject.get(Nonce.getKey()).getAsLong();
    JsonArray bids = jsonObject.getAsJsonArray(JsonKey.Buys.getKey());
    JsonArray asks = jsonObject.getAsJsonArray(JsonKey.Sells.getKey());
    return ExchangeDeltaEvent.parse(market, sequence, bids, asks);
  }
}