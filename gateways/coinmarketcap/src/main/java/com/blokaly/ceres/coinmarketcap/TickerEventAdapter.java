package com.blokaly.ceres.coinmarketcap;

import com.blokaly.ceres.common.DecimalNumber;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class TickerEventAdapter implements JsonDeserializer<TickerEvent> {

  private static Logger LOGGER = LoggerFactory.getLogger(TickerEventAdapter.class);

  @Override
  public TickerEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject tickerJson = json.getAsJsonObject();
    String symbol = tickerJson.get("symbol").getAsString();
    JsonElement priceElm = tickerJson.get("price_usd");
    DecimalNumber priceUsd = priceElm.isJsonNull() ? DecimalNumber.ZERO : DecimalNumber.fromStr(priceElm.getAsString());
    JsonElement lastUpdateElm = tickerJson.get("last_updated");
    long lastUpdate = lastUpdateElm.isJsonNull() ?  0L : lastUpdateElm.getAsLong();
    return new TickerEvent(symbol, priceUsd, lastUpdate);
  }
}