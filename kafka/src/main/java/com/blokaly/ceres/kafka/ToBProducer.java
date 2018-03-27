package com.blokaly.ceres.kafka;

import com.blokaly.ceres.orderbook.TopOfBook;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class ToBProducer {
  private static Logger LOGGER = LoggerFactory.getLogger(ToBProducer.class);
  private static final String KAFKA_TOPIC = "kafka.topic";
  private final Producer<String, String> producer;
  private final Gson gson;
  private final String topic;
  private final ConcurrentMap<String, Integer> hashCache;
  private volatile boolean closing = false;


  @Inject
  public ToBProducer(Producer<String, String> producer, Gson gson, Config config) {
    this.producer = producer;
    this.gson = gson;
    topic = config.getString(KAFKA_TOPIC);
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

    String[] bid = topOfBook.topOfBids();
    String[] ask = topOfBook.topOfAsks();
    Integer hash = 31 * Arrays.hashCode(bid) + Arrays.hashCode(ask);
    String key = topOfBook.getKey();
    Integer last = hashCache.get(key);
    if (!hash.equals(last) ) {
      ArrayList<List<String[]>> tob = new ArrayList<>();
      tob.add(Collections.singletonList(bid));
      tob.add(Collections.singletonList(ask));
      dispatch(key, gson.toJson(tob));
      hashCache.put(key, hash);
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
