package com.blokaly.ceres.jedis;

import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.redis.RedisClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class SingleJedis implements RedisClient {
  private static Logger LOGGER = LoggerFactory.getLogger(SingleJedis.class);
  private final Jedis jedis;
  private final ExecutorService executor;

  @Inject
  public SingleJedis(Jedis jedis, @SingleThread ExecutorService executor) {

    this.jedis = jedis;
    this.executor = executor;
  }

  @Override
  public void set(String key, String value) {
    executor.execute(() -> {
      jedis.set(key, value);
    });
  }

  @Override
  public void set(String key, String value, int ttl) {
    executor.execute(() -> {
      jedis.set(key, value);
      if (ttl > 0) {
        jedis.expire(key, ttl);
      }
    });
  }

  @Override
  public String get(String key) {
    try {
      return executor.submit(() -> jedis.get(key)).get(200, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      LOGGER.error("Failed to retrieve value from redis for key:" + key, e);
      return null;
    }
  }

}
