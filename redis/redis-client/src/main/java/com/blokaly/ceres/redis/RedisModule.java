package com.blokaly.ceres.redis;

import com.blokaly.ceres.jedis.JedisProvider;
import com.blokaly.ceres.jedis.SingleJedis;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import redis.clients.jedis.Jedis;

public class RedisModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Jedis.class).toProvider(JedisProvider.class).in(Singleton.class);
    bind(RedisClient.class).to(SingleJedis.class).in(Singleton.class);
  }
}
