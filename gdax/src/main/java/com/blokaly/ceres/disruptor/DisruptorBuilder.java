package com.blokaly.ceres.disruptor;

import com.blokaly.ceres.concurrent.ExecutorServiceBuilder;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

public class DisruptorBuilder {

    public static <T> Disruptor<T> createDisruptor(String name, EventFactory<T> eventFactory, int size, ProducerType producerType, WaitStrategy waitStrategy, EventHandler<T>... handlers) {

        ThreadFactory threadFactory = ExecutorServiceBuilder.threadFactory(name);

        Disruptor<T> disruptor = new Disruptor<>(eventFactory, size, threadFactory, producerType, waitStrategy);
        disruptor.handleEventsWith(handlers);
        for (EventHandler<T> handler : handlers) {
            disruptor.handleExceptionsFor(handler).with(exceptionHandler(name, handler.getClass().getName()));
        }
        return disruptor;
    }

    private static <T> ExceptionHandler<T> exceptionHandler(String disruptorName, String handlerName) {

        return new ExceptionHandler<T>() {
            private final Logger logger = LoggerFactory.getLogger(getClass());

            @Override
            public void handleEventException(Throwable ex, long sequence, Object event) {
                String format = String.format("Disruptor[%s-%s] event[%s] handling exception", disruptorName, handlerName, sequence);
                logger.error(format, ex);
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                logger.error("Disruptor["+disruptorName+"] start exception", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                logger.error("Disruptor["+disruptorName+"] shutdown exception", ex);
            }
        };
    }
}
