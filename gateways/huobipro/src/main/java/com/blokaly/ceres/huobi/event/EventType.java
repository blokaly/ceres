package com.blokaly.ceres.huobi.event;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
  OPEN("open"), CLOSE("close"),
  PING("ping"), PONG("pong"),
  SUBSCRIBE("sub"), SUBSCRIBED("subbed"), TICK("tick");

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
