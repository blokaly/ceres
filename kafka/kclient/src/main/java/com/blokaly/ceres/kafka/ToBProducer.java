package com.blokaly.ceres.kafka;

import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.orderbook.TopOfBook;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public class ToBProducer {
  private static Logger LOGGER = LoggerFactory.getLogger(ToBProducer.class);
  private static final int BOOK_DEPTH = 5;
  private final Producer<String, String> producer;
  private final Gson gson;
  private final String topic;
  private final ConcurrentMap<String, Integer> hashCache;
  private volatile boolean closing = false;


  @Inject
  public ToBProducer(Producer<String, String> producer, Gson gson, Config config) {
    this.producer = producer;
    this.gson = gson;
    topic = config.getString(CommonConfigs.KAFKA_TOPIC);
    hashCache = Maps.newConcurrentMap();
  }

  @PreDestroy
  public void stop() {
    closing = true;
    producer.flush();
    producer.close();
  }

  public void publish(TopOfBook topOfBook) {
    if (closing) {
      return;
    }

    TopOfBook.Entry[] bids = topOfBook.topOfBids(BOOK_DEPTH);
    TopOfBook.Entry[] asks = topOfBook.topOfAsks(BOOK_DEPTH);

    int bidHash = Arrays.hashCode(bids);
    int ashHash = Arrays.hashCode(asks);
    Integer hash = Objects.hash(bidHash, ashHash);
    String key = topOfBook.getKey();
    Integer last = hashCache.get(key);
    if (!hash.equals(last) ) {
      JsonArray tob = new JsonArray();
      JsonArray bidEntries = new JsonArray();
      for (TopOfBook.Entry bid : bids) {
        addEntry(bidEntries, bid);
      }
      tob.add(bidEntries);

      JsonArray askEntries = new JsonArray();
      for (TopOfBook.Entry ask : asks) {
        addEntry(askEntries, ask);
      }
      tob.add(askEntries);
      dispatch(key, gson.toJson(tob));

      hashCache.put(key, hash);
    }
  }

  private void addEntry(JsonArray entries, TopOfBook.Entry entry) {
    if (entry != null) {
      JsonArray array = new JsonArray();
      array.add(entry.price);
      array.add(entry.total);
      entries.add(array);
    }
  }

  private void dispatch(String key, String message) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
    LOGGER.debug("publishing -> {}:{}", key, message);
    producer.send(record, (metadata, exception) -> {
      if (exception != null) {
        LOGGER.error("Error sending Kafka message", exception);
      }
    });
  }
}
