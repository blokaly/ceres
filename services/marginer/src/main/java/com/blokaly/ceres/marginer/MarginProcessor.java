package com.blokaly.ceres.marginer;

import com.blokaly.ceres.common.DecimalNumber;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.joda.time.DateTimeUtils;

import java.math.RoundingMode;

public class MarginProcessor implements KeyValueMapper<String, String, KeyValue<String, String>> {

  private final IDSequencer sequencer;
  private final Gson gson;

  public MarginProcessor(IDSequencer sequencer, Gson gson) {
    this.sequencer = sequencer;
    this.gson = gson;
  }

  @Override
  public KeyValue<String, String> apply(String key, String value) {
    String symbol = key.substring(0, 7);
    return KeyValue.pair(symbol + sequencer.incrementAndGet(), applyMargin(value));
  }

  private String applyMargin(String top) {
    JsonArray prices = gson.fromJson(top, JsonArray.class);
    JsonArray bid = prices.get(0).getAsJsonArray();
    JsonArray ask = prices.get(1).getAsJsonArray();

    JsonArray entry = new JsonArray();
    DecimalNumber bidPrice = bid.size() > 0 ? DecimalNumber.fromStr(bid.get(0).getAsString()) : DecimalNumber.ZERO;
    DecimalNumber askPrice = ask.size() > 0 ? DecimalNumber.fromStr(ask.get(0).getAsString()) : DecimalNumber.ZERO;
    if (!(bidPrice.isZero() || askPrice.isZero())) {
      if (bidPrice.compareTo(askPrice) > 0) {
        DecimalNumber mid = bidPrice.plus(askPrice).halve();
        bidPrice = mid.setScale(bidPrice.getScale(), RoundingMode.HALF_DOWN);
        askPrice = mid.setScale(askPrice.getScale(), RoundingMode.HALF_UP);
      }
    }
    entry.add(DateTimeUtils.currentTimeMillis());
    entry.add(bidPrice.toString());
    entry.add(askPrice.toString());
    entry.add(bid);
    entry.add(ask);

    return gson.toJson(entry);
  }
}
