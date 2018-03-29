package com.blokaly.ceres.margin;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

public class MarginProcessor implements KeyValueMapper<String, String, KeyValue<String, String>> {

  private final IDSequencer sequencer;

  public MarginProcessor(IDSequencer sequencer) {
    this.sequencer = sequencer;
  }

  @Override
  public KeyValue<String, String> apply(String key, String value) {
    String symbol = key.substring(0, 7);
    return KeyValue.pair(symbol + sequencer.incrementAndGet(), value);
  }

}
