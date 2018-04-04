package com.blokaly.ceres.gdax.event;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
  HB("heartbeat"),
  SUBS("subscriptions"),
  SNAPSHOT("snapshot"),
  L2U("l2update");

  private static final Map<String, EventType> lookup = new HashMap<String, EventType>();

  static {
    for (EventType type : EventType.values()) {
      lookup.put(type.getType(), type);
    }
  }

  private final String type;
  private EventType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static EventType get(String type) {
    return lookup.get(type.toLowerCase());
  }
}
