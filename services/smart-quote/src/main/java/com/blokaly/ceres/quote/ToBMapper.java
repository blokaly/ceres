package com.blokaly.ceres.quote;

import com.blokaly.ceres.orderbook.TopOfBook;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Singleton
public class ToBMapper implements KeyValueMapper<String, String, KeyValue<String, String>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ToBMapper.class);
  private final Map<String, BestTopOfBook> books;
  private final Gson gson;

  @Inject
  public ToBMapper(Map<String, BestTopOfBook> books, Gson gson) {
    this.books = books;
    this.gson = gson;
  }

  @Override
  public synchronized KeyValue<String, String> apply(String key, String value) {
    LOGGER.debug("mapping {} -> {}", key, value);
    String[] symex = key.split("\\.");
    JsonArray tob = gson.fromJson(value, JsonArray.class);
    JsonArray bid = tob.get(0).getAsJsonArray();
    JsonArray ask = tob.get(1).getAsJsonArray();

    JsonArray topBid = bid.size() == 0 ? null : bid.get(0).getAsJsonArray();
    JsonArray topAsk = ask.size() == 0 ? null : ask.get(0).getAsJsonArray();

    JsonOrderBook book = JsonOrderBook.parse(symex[1], topBid, topAsk);
    BestTopOfBook topOfBook = books.get(symex[0]);
    topOfBook.processSnapshot(book);

    JsonArray message = new JsonArray();


    TopOfBook.Entry entry = topOfBook.topOfBids(1)[0];
    JsonArray bidEntry = new JsonArray();
    if (entry != null) {
      bidEntry.add(entry.price);
      bidEntry.add(entry.total);
      bidEntry.add(gson.toJsonTree(entry.quantities));
    }
    message.add(bidEntry);

    entry = topOfBook.topOfAsks(1)[0];
    JsonArray askEntry = new JsonArray();
    if (entry != null) {
      askEntry.add(entry.price);
      askEntry.add(entry.total);
      askEntry.add(gson.toJsonTree(entry.quantities));
    }
    message.add(askEntry);

    return KeyValue.pair(topOfBook.getKey(), gson.toJson(message));
  }

  public synchronized void remove(List<String> staled) {
    books.values().forEach(bestTopOfBook -> bestTopOfBook.remove(staled));
  }
}
