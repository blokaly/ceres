package com.blokaly.ceres.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

public class ExecutorServiceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceBuilder.class);


    public static ThreadFactory threadFactory(String name) {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("dsl-"+name+"-%d");
        builder.setDaemon(true);
        builder.setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
        return builder.build();
    }

    private static class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error(t.getName() + " UncaughtException", e);
        }
    }

}
