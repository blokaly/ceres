package com.blokaly.ceres.kafka;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

public class TextProducer {
  private static Logger LOGGER = LoggerFactory.getLogger(TextProducer.class);
  private final Producer<String, String> producer;
  private final String topic;
  private volatile boolean closing = false;

  @Inject
  public TextProducer(Producer<String, String> producer, Config config) {
    this.producer = producer;
    topic = config.getString(CommonConfigs.KAFKA_TOPIC);
  }

  @PreDestroy
  public void stop() {
    closing = true;
    producer.flush();
    producer.close();
  }

  public void publish(String key, String text) {
    if (closing) {
      return;
    }

    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, text);
    LOGGER.debug("publishing -> {}", text);
    producer.send(record, (metadata, exception) -> {
      if (metadata==null || exception != null) {
        LOGGER.error("Error sending Kafka message", exception);
      }
    });
  }
}
