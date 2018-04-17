package com.blokaly.ceres.redis;

import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.jedis.JedisProvider;
import com.blokaly.ceres.jedis.SingleJedis;
import com.google.inject.Singleton;
import redis.clients.jedis.Jedis;

public class RedisModule extends CeresModule {

  @Override
  protected void configure() {
    bindExpose(JedisProvider.class);
    bind(Jedis.class).toProvider(JedisProvider.class).in(Singleton.class);
    bindExpose(RedisClient.class).to(SingleJedis.class).in(Singleton.class);
  }
}
