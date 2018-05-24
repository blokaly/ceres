package com.blokaly.ceres.cex.event;

public class PingEvent extends AbstractEvent{
  private long time;
  public PingEvent() {
    super(EventType.PING.getType());
  }

  @Override
  public String toString() {
    return "PingEvent{" +
        "time=" + time +
        ", e=" + e +
        '}';
  }
}
