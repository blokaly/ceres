package com.blokaly.ceres.okcoin.event;

public class OpenEvent implements ChannelEvent {
  @Override
  public EventType getType() {
    return EventType.OPEN;
  }
}