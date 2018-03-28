package com.blokaly.ceres.bitfinex.event;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
  HB("hb"), INFO("info"), CHANNEL("channel"), SUBSCRIBED("subscribed"), SNAPSHOT("snapshot"), REFRESH("refresh");

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
