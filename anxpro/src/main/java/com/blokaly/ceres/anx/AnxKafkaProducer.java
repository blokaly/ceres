package com.blokaly.ceres.anx;

import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AnxKafkaProducer {
  private static Logger LOGGER = LoggerFactory.getLogger(AnxKafkaProducer.class);
  private final Producer<String, String> producer;
  private final Gson gson;
  private final String topic;
  private volatile boolean closing = false;

  @Inject
  public AnxKafkaProducer(Producer<String, String> producer, Gson gson, Config config) {
    this.producer = producer;
    this.gson = gson;
    topic = config.getString("kafka.topic");
  }

  @PreDestroy
  public void stop() {
    closing = true;
    producer.flush();
    producer.close();
  }

  public void publish(PriceBasedOrderBook orderBook) {

    if (closing) {
      return;
    }

    ArrayList<List<String[]>> tob = new ArrayList<>();
    tob.add(orderBook.topOfBids(1));
    tob.add(orderBook.topOfAsks(1));
    send(orderBook.getSymbol(), gson.toJson(tob));

  }

  private void send(String symbol, String message) {

    LOGGER.info("{}: {}", symbol, message);
//    ProducerRecord<String, String> record = new ProducerRecord<>(topic, symbol.toLowerCase(), message);
//    producer.send(record, (metadata, exception) -> {
//      if (exception != null) {
//        LOGGER.error("Error sending Kafka message", exception);
//      }
//    });
  }
}
