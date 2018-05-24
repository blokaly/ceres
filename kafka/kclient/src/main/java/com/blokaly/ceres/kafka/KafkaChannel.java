package com.blokaly.ceres.kafka;

import com.blokaly.ceres.common.SingleThread;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Singleton
public class KafkaChannel implements Channel<String> {
  private static Logger LOGGER = LoggerFactory.getLogger(KafkaChannel.class);
  private final Consumer<String, String> consumer;
  private final ExecutorService executorService;
  private volatile boolean enabled;

  @Inject
  public KafkaChannel(Consumer<String, String> consumer, @SingleThread ExecutorService executorService) {
    this.consumer = consumer;
    this.executorService = executorService;
    enabled = false;
  }

  @Override
  public  Subscription<String> subscribe(Subscriber<String> subscriber, String... topics) {
    Subscription<String> subscription = new KafkaConsumerSubscription();
    subscription.add(subscriber);
    if (!enabled) {
      enabled = true;
      consumer.subscribe(Arrays.asList(topics));
      open(subscription);
    }
    return subscription;
  }

  private void open(Subscription<String> subscription) {
    executorService.submit(()->{
      while (enabled) {
        try {
          ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
          for (ConsumerRecord<String, String> record : records) {
            subscription.onUpdate(record.topic(), record.key(), record.value());
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

  private class KafkaConsumerSubscription implements Subscription<String> {
    private final Set<Subscriber<String>> subscribers = Sets.newCopyOnWriteArraySet();

    @Override
    public void add(Subscriber<String> subscriber) {
      subscribers.add(subscriber);
    }

    @Override
    public void onUpdate(String topic, String key, String value) {
      for (Subscriber<String> subscriber : subscribers) {
        subscriber.onSubscription(topic, key, value);
      }
    }

    @Override
    public void remove(Subscriber<String> subscriber) {
      subscribers.remove(subscriber);
    }
  }
}
