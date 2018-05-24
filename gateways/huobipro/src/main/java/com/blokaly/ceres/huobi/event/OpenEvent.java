package com.blokaly.ceres.huobi.event;

public class OpenEvent implements WSEvent {
  @Override
  public EventType getType() {
    return EventType.OPEN;
  }
}