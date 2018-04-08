package com.blokaly.ceres.quote;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.jedis.JedisProvider;
import com.blokaly.ceres.redis.RedisModule;
import com.blokaly.ceres.web.HandlerModule;
import com.blokaly.ceres.web.UndertowModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorService extends BootstrapService {
  private static Logger LOGGER = LoggerFactory.getLogger(ValidatorService.class);
  private final Undertow server;

  @Inject
  public ValidatorService(Undertow server) {
    this.server = server;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("Web server starting...");
    server.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("Web server stopping...");
    server.stop();
  }

  private static class QuoteValidatorModule extends AbstractModule {

    @Override
    protected void configure() {
      install(new CommonModule());
      install(new RedisModule());
      install(new UndertowModule(new HandlerModule() {
        @Override
        protected void configureHandlers() {
          bindHandler().to(QuoteQueryHandler.class);
        }
      }));
    }
  }

  public static void main(String[] args) {
    Services.start(new QuoteValidatorModule());
  }
}
