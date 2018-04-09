package com.blokaly.ceres.quote;

import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.kafka.Channel;
import com.blokaly.ceres.kafka.KafkaChannel;
import com.blokaly.ceres.redis.RedisClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Singleton
public class QuoteStore implements Channel.Subscriber<ConsumerRecord<String,String>> {
  private static final int EXPIRE_SECONDS = 30;
  private final KafkaChannel channel;
  private final RedisClient redis;
  private final String topic;
  private Channel.Subscription<ConsumerRecord<String, String>> subscription;

  @Inject
  public QuoteStore(KafkaChannel channel, RedisClient redis, Config config) {
    this.channel = channel;
    this.redis = redis;
    topic = config.getString(CommonConfigs.KAFKA_TOPIC);
  }

  public synchronized void start() {
    channel.open();
    subscription = channel.subscribe(topic, this);
  }

  public synchronized void stop() {
    if (subscription != null) {
      subscription.remove(this);
    }
    channel.close();
  }

  @Override
  public String getId() {
    return "QuoteStore."+topic;
  }

  @Override
  public void onSubscription(String topic, ConsumerRecord<String, String> item) {
    redis.set(item.key(), item.value(), EXPIRE_SECONDS);
  }
}
