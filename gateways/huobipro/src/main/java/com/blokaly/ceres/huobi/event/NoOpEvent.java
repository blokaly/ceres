package com.blokaly.ceres.huobi.event;

public class NoOpEvent implements WSEvent {
  @Override
  public EventType getType() {
    return null;
  }
}
