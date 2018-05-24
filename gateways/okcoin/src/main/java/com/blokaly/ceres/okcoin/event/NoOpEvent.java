package com.blokaly.ceres.okcoin.event;

public class NoOpEvent implements ChannelEvent {

  @Override
  public EventType getType() {
    return EventType.NOOP;
  }
}
