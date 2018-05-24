package com.blokaly.ceres.quote;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.jedis.JedisProvider;
import com.blokaly.ceres.kafka.KafkaChannel;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.redis.RedisClient;
import com.blokaly.ceres.redis.RedisModule;
import com.blokaly.ceres.web.HandlerModule;
import com.blokaly.ceres.web.UndertowModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;

public class ValidatorService extends BootstrapService {
  private static Logger LOGGER = LoggerFactory.getLogger(ValidatorService.class);
  private final Undertow server;
  private final QuoteStore store;

  @Inject
  public ValidatorService(Undertow server, QuoteStore store) {
    this.server = server;
    this.store = store;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("Web server starting...");
    server.start();
    store.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("Web server stopping...");
    server.stop();
    store.stop();
  }

  private static class QuoteValidatorModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      install(new RedisModule());
      install(new UndertowModule(new HandlerModule() {
        @Override
        protected void configureHandlers() {
          bindHandler().to(QuoteQueryHandler.class);
        }
      }));

      expose(RedisClient.class);
      expose(JedisProvider.class);
      expose(Undertow.class);
      bindExpose(KafkaChannel.class);
    }
  }

  public static void main(String[] args) {
    Services.start(new QuoteValidatorModule());
  }
}
