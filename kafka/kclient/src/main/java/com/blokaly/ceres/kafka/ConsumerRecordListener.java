package com.blokaly.ceres.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ConsumerRecordListener<K, V> {
  abstract class StringRecordAdapter implements ConsumerRecordListener<String, String>{};
  void onReceive(ConsumerRecord<K, V> record);
}
