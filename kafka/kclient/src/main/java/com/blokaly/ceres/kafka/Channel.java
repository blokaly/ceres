package com.blokaly.ceres.kafka;

public interface Channel<T> {

  interface Subscription<T> {
    void onUpdate(String topic, String key, T value);
    void add(Subscriber<T> subscriber);
    void remove(Subscriber<T> subscriber);
  }

  interface Subscriber<T> {
    void onSubscription(String topic, String key, T value);
  }

  Subscription<T> subscribe(Subscriber<T> subscriber, String... topics);
}
