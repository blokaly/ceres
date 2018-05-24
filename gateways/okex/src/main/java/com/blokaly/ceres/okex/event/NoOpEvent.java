package com.blokaly.ceres.okex.event;

public class NoOpEvent implements ChannelEvent {

  @Override
  public EventType getType() {
    return EventType.NOOP;
  }
}
