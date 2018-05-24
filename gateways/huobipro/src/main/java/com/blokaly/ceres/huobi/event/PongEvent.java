package com.blokaly.ceres.huobi.event;

public class PongEvent implements WSEvent {
  private long pong;

  public PongEvent(long timestamp) {
    this.pong = timestamp;
  }

  @Override
  public EventType getType() {
    return EventType.PONG;
  }

  public long getPong() {
    return pong;
  }

  @Override
  public String toString() {
    return "PongEvent{" +
        "pong=" + pong +
        '}';
  }
}
