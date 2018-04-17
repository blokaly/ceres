package com.blokaly.ceres.bitfinex.event;

public class PingEvent extends AbstractEvent {

  public PingEvent() {
    super("ping");
  }

  @Override
  public String toString() {
    return "Ping";
  }
}
