package com.blokaly.ceres.bitfinex.event;

public class PongEvent extends AbstractEvent {

  public PongEvent() {
    super("pong");
  }

  @Override
  public String toString() {
    return "Pong";
  }
}
