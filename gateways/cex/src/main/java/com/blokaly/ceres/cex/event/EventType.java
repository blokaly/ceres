package com.blokaly.ceres.cex.event;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
  OPEN("open"), CLOSE("close"), CONNECTED("connected"), PING("ping"), PONG("pong"),
  AUTH("auth"), SUBSCRIBE("order-book-subscribe"), UPDATE("md_update");

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
    if (type == null) {
      return null;
    } else {
      return lookup.get(type.toLowerCase());
    }
  }
}
