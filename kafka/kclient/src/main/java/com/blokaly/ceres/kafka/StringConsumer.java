package com.blokaly.ceres.kafka;

import com.blokaly.ceres.common.CommonConfigs;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Singleton
public class StringConsumer {
  private static Logger LOGGER = LoggerFactory.getLogger(StringConsumer.class);
  private final Consumer<String, String> consumer;
  private final ConsumerRecordListener<String, String> listener;
  private final String topic;
  private volatile boolean stop;

  @Inject
  public StringConsumer(Consumer<String, String> consumer, Config config, ConsumerRecordListener<String, String> listener) {
    this.consumer = consumer;
    this.listener = listener;
    topic = config.getString(CommonConfigs.KAFKA_TOPIC);
    stop = false;
  }

  public void start() {
    consumer.subscribe(Collections.singletonList(topic));
    process();
  }

  private void process() {
    while (!stop) {
      try {
        ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<String, String> record : records) {
          listener.onReceive(record);
        }
        consumer.commitSync();
      } catch(WakeupException wex) {
        // ignore
      } catch (Exception ex) {
        LOGGER.error("Error processing records", ex);
      }
    }
    LOGGER.info("Consumer closing");
    consumer.close();
  }

  public void stop() {
    stop = true;
    consumer.wakeup();
  }
}
