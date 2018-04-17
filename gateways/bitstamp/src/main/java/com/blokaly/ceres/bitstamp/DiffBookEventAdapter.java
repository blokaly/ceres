package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class DiffBookEventAdapter implements JsonDeserializer<DiffBookEvent> {

    private static Logger LOGGER = LoggerFactory.getLogger(DiffBookEventAdapter.class);

    @Override
    public DiffBookEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        long sequence = jsonObject.get("timestamp").getAsLong();
        JsonArray bids = jsonObject.get("bids").getAsJsonArray();
        JsonArray asks = jsonObject.get("asks").getAsJsonArray();
        return DiffBookEvent.parse(sequence, bids, asks);
    }
}
