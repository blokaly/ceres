package com.blokaly.ceres.quote;

import org.apache.kafka.streams.kstream.Predicate;

class SymbolFilter implements Predicate<String, String> {
  private final String sym;

  public SymbolFilter(String sym) {
    this.sym = sym;
  }

  @Override
  public boolean test(String key, String value) {
    return key.startsWith(sym);
  }
}
