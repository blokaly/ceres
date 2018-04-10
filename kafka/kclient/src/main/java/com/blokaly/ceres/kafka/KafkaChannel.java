package com.blokaly.ceres.kafka;

import com.blokaly.ceres.common.SingleThread;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Singleton
public class KafkaChannel implements Channel<ConsumerRecord<String, String>> {
  private static Logger LOGGER = LoggerFactory.getLogger(KafkaChannel.class);
  private final Consumer<String, String> consumer;
  private final ExecutorService executorService;
  private final ConcurrentMap<String, Subscription<ConsumerRecord<String, String>>> subscriptions;
  private volatile boolean enabled;

  @Inject
  public KafkaChannel(Consumer<String, String> consumer, @SingleThread ExecutorService executorService) {
    this.consumer = consumer;
    this.executorService = executorService;
    enabled = false;
    subscriptions = Maps.newConcurrentMap();
  }

  @Override
  public  Subscription<ConsumerRecord<String, String>> subscribe(String topic, Subscriber<ConsumerRecord<String, String>> subscriber) {
    Subscription<ConsumerRecord<String, String>> subscription = subscriptions.computeIfAbsent(topic, this::makeSubscription);
    subscription.add(subscriber);
    if (!enabled) {
      open();
    }
    return subscription;
  }

  private void open() {
    enabled = true;
    executorService.submit(()->{
      while (enabled) {
        try {
          ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
          for (ConsumerRecord<String, String> record : records) {
            subscriptions.get(record.topic()).onUpdate(record);
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
    });
  }

  public void close() {
    enabled = false;
    consumer.wakeup();
  }

  private Subscription<ConsumerRecord<String, String>> makeSubscription(String topic) {
    consumer.subscribe(Collections.singletonList(topic));
    return new KafkaConsumerSubscription(topic);
  }

  private class KafkaConsumerSubscription implements Subscription<ConsumerRecord<String, String>> {
    private final String topic;
    private final ConcurrentMap<String, Subscriber<ConsumerRecord<String, String>>> subscribers;

    private KafkaConsumerSubscription(String topic) {
      this.topic = topic;
      subscribers = Maps.newConcurrentMap();
    }

    @Override
    public String getId() {
      return topic;
    }

    @Override
    public void add(Subscriber<ConsumerRecord<String, String>> subscriber) {
      subscribers.putIfAbsent(subscriber.getId(), subscriber);
    }

    @Override
    public void onUpdate(ConsumerRecord<String, String> item) {
      for (Subscriber<ConsumerRecord<String, String>> subscriber : subscribers.values()) {
        subscriber.onSubscription(getId(), item);
      }
    }

    @Override
    public void remove(Subscriber<ConsumerRecord<String, String>> subscriber) {
      subscribers.remove(subscriber.getId());
    }
  }
}
