package com.blokaly.ceres.stream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class ToBMapper implements KeyValueMapper<String, String, KeyValue<String, String>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ToBMapper.class);
  private final Map<String, AggregatedTopOfBook> books;
  private final Gson gson;

  @Inject
  public ToBMapper(Map<String, AggregatedTopOfBook> books, Gson gson) {
    this.books = books;
    this.gson = gson;
  }

  @Override
  public synchronized KeyValue<String, String> apply(String key, String value) {
    LOGGER.debug("mapping {} -> {}", key, value);
    String[] symex = key.split("\\.");
    JsonArray tob = gson.fromJson(value, JsonArray.class);
    JsonArray bids = tob.get(0).getAsJsonArray();
    JsonArray asks = tob.get(1).getAsJsonArray();
    JsonOrderBook book = JsonOrderBook.parse(symex[1], bids, asks);
    AggregatedTopOfBook aggregatedBook = books.get(symex[0]);
    aggregatedBook.processSnapshot(book);
    ArrayList<List<String[]>> message = new ArrayList<>();
    message.add(Collections.singletonList(aggregatedBook.topOfBids()));
    message.add(Collections.singletonList(aggregatedBook.topOfAsks()));
    return KeyValue.pair(aggregatedBook.getKey(), gson.toJson(message));
  }

  public synchronized void remove(List<String> staled) {
    books.values().forEach(bestTopOfBook -> bestTopOfBook.remove(staled));
  }
}
