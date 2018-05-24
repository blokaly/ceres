package com.blokaly.ceres.binance;

import com.blokaly.ceres.binance.event.DiffBookEvent;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class DiffBookEventAdapter implements JsonDeserializer<DiffBookEvent> {

    private static Logger LOGGER = LoggerFactory.getLogger(DiffBookEventAdapter.class);

    @Override
    public DiffBookEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        long begin = jsonObject.get("U").getAsLong();
        long end = jsonObject.get("u").getAsLong();
        JsonArray bids = jsonObject.get("b").getAsJsonArray();
        JsonArray asks = jsonObject.get("a").getAsJsonArray();
        return DiffBookEvent.parse(begin, end, bids, asks);
    }
}
