package com.blokaly.ceres.gdax.event;

import java.util.Arrays;

public class SubscribedEvent extends AbstractEvent {

  private Channel[] channels;

  @Override
  public String toString() {
    return "SubscribedEvent{" +
        "channels=" + Arrays.toString(channels) +
        '}';
  }
}
