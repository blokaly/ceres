package com.blokaly.ceres.anx.event;

public abstract class AbstractEvent {

  public String event;

  AbstractEvent() {}

  AbstractEvent(String event) {
    this.event = event;
  }

  public String getEvent() {
    return event;
  }

}