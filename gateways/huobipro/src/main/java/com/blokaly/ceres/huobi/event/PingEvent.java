package com.blokaly.ceres.huobi.event;

public class PingEvent implements WSEvent {
  private long ping;

  public PingEvent(long timestamp) {
    this.ping = timestamp;
  }

  @Override
  public EventType getType() {
    return EventType.PING;
  }

  public long getPing() {
    return ping;
  }

  @Override
  public String toString() {
    return "PingEvent{" +
        "ping=" + ping +
        '}';
  }
}
