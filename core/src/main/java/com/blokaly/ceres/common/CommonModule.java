package com.blokaly.ceres.common;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.*;
import com.typesafe.config.Config;

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
