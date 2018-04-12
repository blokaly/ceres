package com.blokaly.ceres.common;

import com.blokaly.ceres.health.HealthLogger;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.*;

import static com.blokaly.ceres.common.Configs.BOOLEAN_EXTRACTOR;
import static com.blokaly.ceres.common.Configs.STRING_EXTRACTOR;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new DumpAndShutdownModule());
    bind(Thread.UncaughtExceptionHandler.class).to(ExceptionLoggingHandler.class).in(Singleton.class);
    bind(Config.class).toProvider(Configs::getConfig).in(Singleton.class);
    bind(StdRedirect.class).asEagerSingleton();
  }

  @Provides @Singleton
  public ThreadFactory provideThreadFactory(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
    ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
    builder.setNameFormat("ceres-%d");
    builder.setDaemon(true).setUncaughtExceptionHandler(uncaughtExceptionHandler);
    return builder.build();
  }

  @Provides @SingleThread
  public ScheduledExecutorService provideSingleScheduledExecutorService(ThreadFactory factory) {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(factory);
    MoreExecutors.addDelayedShutdownHook(service, 5, TimeUnit.SECONDS);
    return service;
  }

  @Provides @SingleThread
  public ExecutorService provideSingleExecutorService(ThreadFactory factory) {
    ExecutorService service = Executors.newSingleThreadExecutor(factory);
    MoreExecutors.addDelayedShutdownHook(service, 5, TimeUnit.SECONDS);
    return service;
  }

  @Provides
  @Singleton
  public HealthCheckRegistry provideHealthCheckRegistry(@SingleThread ScheduledExecutorService ses) {
    HealthCheckRegistry registry = new HealthCheckRegistry();
    ses.scheduleWithFixedDelay(() -> {
      for (Map.Entry<String, HealthCheck.Result> entry : registry.runHealthChecks().entrySet()) {
        String name = entry.getKey();
        Logger logger = HealthLogger.getLogger(name);
        HealthCheck.Result result = entry.getValue();
        if (result.isHealthy()) {
          logger.info("Service OK");
        } else {
          logger.error("Service FAIL - {}", result.getMessage());
        }
      }
    }, 5, 5, TimeUnit.SECONDS);
    return registry;
  }

  @Singleton
  public static class StdRedirect {
    @Inject
    public StdRedirect(Config config) throws FileNotFoundException {
      if (Configs.getOrDefault(config, "std.redirect", BOOLEAN_EXTRACTOR, false)) {
        String logRoot = Configs.getOrDefault(config, "log.root", STRING_EXTRACTOR, ".");
        System.setOut(new PrintStream(logRoot + "/std.out"));
        System.setErr(new PrintStream(logRoot + "/std.err"));
      }
    }
  }

}
