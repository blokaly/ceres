package com.blokaly.ceres.kafka;

public interface Channel<T> {

  interface Subscription<T> {
    String getId();
    void onUpdate(T item);
    void unsubscribe(Subscriber<T> subscriber);
  }

  interface Subscriber<T> {
    void onSubscription(String subscriptionId, T item);
  }

  Subscription<T> subscribe(String topic, Subscriber<T> subscriber);
}
