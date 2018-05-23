package com.blokaly.ceres.okex.event;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public class EventAdapter implements JsonDeserializer<ChannelEvent> {

  private static Logger LOGGER = LoggerFactory.getLogger(EventAdapter.class);

  private final NoOpEvent noOpEvent = new NoOpEvent();


  @Override
  public ChannelEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    JsonArray array = json.getAsJsonArray();
    if (array.size() == 0) {
      return new NoOpEvent();
    }

    JsonObject jsonObject = array.get(0).getAsJsonObject();
    JsonObject data = jsonObject.get("data").getAsJsonObject();
    if (jsonObject.has("channel")) {
      String channel = jsonObject.get("channel").getAsString();
      if ("addChannel".equals(channel)) {
        return context.deserialize(data, SubscribedEvent.class);
      } else {
        if (data.has("result") && !data.get("result").getAsBoolean()) {
          return context.deserialize(data, ErrorEvent.class);
        } else {
          long sequence = data.get("timestamp").getAsLong();
          JsonArray emtptyArray = new JsonArray();
          JsonArray bids = data.has("bids") ? data.get("bids").getAsJsonArray() : emtptyArray;
          JsonArray asks = data.has("asks") ? data.get("asks").getAsJsonArray() : emtptyArray;
          return MDUpdateEvent.parse(channel, sequence, bids, asks);
        }
      }
    } else {
      return context.deserialize(data, ErrorEvent.class);
    }
  }
}