package com.blokaly.ceres.huobi.event;

public class CloseEvent implements WSEvent {

  @Override
  public EventType getType() {
    return EventType.CLOSE;
  }
}
