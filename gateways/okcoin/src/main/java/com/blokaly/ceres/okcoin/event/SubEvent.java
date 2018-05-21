package com.blokaly.ceres.okcoin.event;

public class SubEvent {
  private final String event = "addChannel";
  private final String channel;
  private final int binary = 0;

  public SubEvent(String channel) {
    this.channel = channel;
  }
}
