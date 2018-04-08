package com.blokaly.ceres.jedis;

import com.blokaly.ceres.binding.ServiceProvider;
import com.blokaly.ceres.common.Configs;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import static com.blokaly.ceres.common.Configs.INTEGER_EXTRACTOR;

@Singleton
public class JedisProvider extends ServiceProvider<Jedis> {
  private static Logger LOGGER = LoggerFactory.getLogger(JedisProvider.class);
  private final Jedis jedis;

  @Inject
  public JedisProvider(Config config) {
    String host = config.getString(JedisConfigs.REDIS_HOST);
    int port = config.getInt(JedisConfigs.REDIS_PORT);
    Integer timeout = Configs.getOrDefault(config, JedisConfigs.REDIS_TIMEOUT, INTEGER_EXTRACTOR, Protocol.DEFAULT_TIMEOUT);
    jedis = new Jedis(host, port, timeout);
  }

  @Override
  public Jedis get() {
    return jedis;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("Jedis starting...");
    jedis.connect();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("Jedis stopping...");
    jedis.close();
  }
}
