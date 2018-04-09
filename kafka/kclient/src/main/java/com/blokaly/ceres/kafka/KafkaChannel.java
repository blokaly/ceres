package com.blokaly.ceres.kafka;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

public class KafkaChannel implements Channel<ConsumerRecords<String, String>> {
  private static Logger LOGGER = LoggerFactory.getLogger(KafkaChannel.class);
  private final Consumer<String, String> consumer;
  private final ConcurrentMap<String, Subscription<ConsumerRecords<String, String>>> subscriptions;
  private volatile boolean stop;

  @Inject
  public KafkaChannel(Consumer<String, String> consumer) {
    this.consumer = consumer;
    stop = false;
    subscriptions = Maps.newConcurrentMap();
  }

  @Override
  public  Subscription<ConsumerRecords<String, String>> subscribe(String topic, Subscriber<ConsumerRecords<String, String>> subscriber) {

    Subscription<ConsumerRecords<String, String>> subscription = subscriptions.getOrDefault(topic, makeSubscription());
    consumer.subscribe(Collections.singletonList(topic));
    return subscription;
  }

  private Subscription<ConsumerRecords<String, String>> makeSubscription() {
    return null;
  }
}
