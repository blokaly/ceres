package com.blokaly.ceres.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionLoggingHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLoggingHandler.class);
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Uncaught exception occurred on thread " + t, e);
    }
}
