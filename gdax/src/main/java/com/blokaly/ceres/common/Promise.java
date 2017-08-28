package com.blokaly.ceres.common;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Promise<T> {

    <V> Promise<V> next(Function<T, V> job);

    void fail(Consumer<Exception> exceptionHandler);
}
