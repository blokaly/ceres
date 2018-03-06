package com.blokaly.ceres.common;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.*;

public class CommonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Thread.UncaughtExceptionHandler.class).to(ExceptionLoggingHandler.class).in(Singleton.class);
    }

    @Provides
    @Singleton
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
    public Config provideConfig() {
        Config defaultConfig = ConfigFactory.parseResources("defaults.conf");
        Config config = ConfigFactory.parseResources("overrides.conf")
                .withFallback(defaultConfig).resolve();
        return config;
    }
}
