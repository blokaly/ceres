package com.blokaly.ceres.okex.event;

public class CloseEvent implements ChannelEvent {

  @Override
  public EventType getType() {
    return EventType.CLOSE;
  }
}
