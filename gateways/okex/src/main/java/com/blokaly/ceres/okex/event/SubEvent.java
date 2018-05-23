package com.blokaly.ceres.okex.event;

public class SubEvent {
  private final String event = "addChannel";
  private final String channel;

  public SubEvent(String channel) {
    this.channel = channel;
  }
}
