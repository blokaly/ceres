package com.blokaly.ceres.marginer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

@Singleton
public class IDSequencer {
  private final String prefix;
  private long sequence;

  @Inject
  public IDSequencer(Config config) {
    prefix = config.getString("app.id.prefix");
    sequence = System.currentTimeMillis() * 1000000L;
  }

  public String incrementAndGet() {
    return prefix + ++sequence;
  }
}
