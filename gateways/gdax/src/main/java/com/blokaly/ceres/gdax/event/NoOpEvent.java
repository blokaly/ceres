package com.blokaly.ceres.gdax.event;

public class NoOpEvent extends AbstractEvent {
  public NoOpEvent() {
    super("noop");
  }
}
