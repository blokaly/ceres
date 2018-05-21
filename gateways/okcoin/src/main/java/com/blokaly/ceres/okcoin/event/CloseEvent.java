package com.blokaly.ceres.okcoin.event;

public class CloseEvent implements ChannelEvent {

  @Override
  public EventType getType() {
    return EventType.CLOSE;
  }
}
