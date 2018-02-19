package com.blokaly.ceres.common;

import com.google.common.util.concurrent.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FullFill<T> implements Promise<T> {

    private final ListeningExecutorService service;
    private ListenableFuture<T> lastResult;

    public static <V> FullFill<V> waterfall(Supplier<V> job) {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        ListenableFuture<V> future = service.submit(job::get);
        return new FullFill<>(service, future);
    }

    private FullFill(ListeningExecutorService service, ListenableFuture<T> lastResult) {
        this.service = service;
        this.lastResult = lastResult;
    }

    @Override
    public <V> Promise<V> next(Function<T, V> job) {
        AsyncFunction<T, V> function = input -> service.submit(() -> job.apply(input));
        ListenableFuture<V> future = Futures.transformAsync(lastResult, function, service);
        return new FullFill<>(service, future);
    }

    @Override
    public void fail(Consumer<Exception> exceptionHandler) {
        Futures.catchingAsync(lastResult, Exception.class, input -> {
            exceptionHandler.accept(input);
            return null;
        }, service);
    }

    public T await() throws ExecutionException, InterruptedException {
        return lastResult.get();
    }

}
