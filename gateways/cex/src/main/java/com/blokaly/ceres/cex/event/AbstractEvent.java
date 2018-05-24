package com.blokaly.ceres.cex.event;

public abstract class AbstractEvent {

  public String e;

  AbstractEvent() {}

  AbstractEvent(String event) {
    this.e = event;
  }

  public String getEvent() {
    return e;
  }

  public EventType getType() {
    return EventType.get(e);
  }
}