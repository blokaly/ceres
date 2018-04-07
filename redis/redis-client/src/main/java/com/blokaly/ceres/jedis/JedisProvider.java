package com.blokaly.ceres.jedis;

import com.blokaly.ceres.common.Configs;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.blokaly.ceres.common.Configs.INTEGER_EXTRACTOR;

@Singleton
public class JedisProvider implements Provider<Jedis> {

  private final Jedis jedis;

  @Inject
  public JedisProvider(Config config) {
    String host = config.getString(JedisConfigs.REDIS_HOST);
    int port = config.getInt(JedisConfigs.REDIS_PORT);
    Integer timeout = Configs.getOrDefault(config, JedisConfigs.REDIS_TIMEOUT, INTEGER_EXTRACTOR, Protocol.DEFAULT_TIMEOUT);
    jedis = new Jedis(host, port, timeout);
  }

  @PostConstruct
  public void start() {
    jedis.connect();
  }

  @Override
  public Jedis get() {
    return jedis;
  }

  @PreDestroy
  public void stop() {
    jedis.close();
  }
}
