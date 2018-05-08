package com.blokaly.ceres.quote;

import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.kafka.Channel;
import com.blokaly.ceres.kafka.KafkaChannel;
import com.blokaly.ceres.redis.RedisClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class QuoteStore implements Channel.Subscriber<String> {
  private static Logger LOGGER = LoggerFactory.getLogger(QuoteStore.class);
  private static final int EXPIRE_SECONDS = 300;
  private final KafkaChannel channel;
  private final RedisClient redis;
  private final String marginTopic;
  private final List<String> symbols;
  private Channel.Subscription<String> subscription;

  @Inject
  public QuoteStore(KafkaChannel channel, RedisClient redis, Config config) {
    this.channel = channel;
    this.redis = redis;
    marginTopic = config.getString(CommonConfigs.KAFKA_TOPIC);
    symbols = config.getStringList("symbols");
  }

  public synchronized void start() {
    String[] topics = new String[symbols.size() + 1];
    topics[0] = marginTopic;
    for (int i = 1; i < topics.length; i++) {
      topics[i] = "md." + symbols.get(i - 1);
    }
    subscription = channel.subscribe(this, topics);
  }

  public synchronized void stop() {
    subscription.remove(this);
    channel.close();
  }

  @Override
  public void onSubscription(String topic, String key, String value) {
    LOGGER.debug("{}: {}->{}", topic, key, value);
    if (marginTopic.equals(topic)) {
      redis.set(key, value, EXPIRE_SECONDS);
    } else {
      // cache the latest value
    }
  }
}
