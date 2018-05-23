package com.blokaly.ceres.okex.event;

public class OpenEvent implements ChannelEvent {
  @Override
  public EventType getType() {
    return EventType.OPEN;
  }
}