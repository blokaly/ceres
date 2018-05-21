package com.blokaly.ceres.okcoin.event;

public class SubscribedEvent implements ChannelEvent {

  private String channel;
  private boolean result;

  @Override
  public EventType getType() {
    return EventType.SUBSCRIBED;
  }

  public boolean isOk() {
    return result;
  }

  public String getChannel() {
    return channel;
  }

  @Override
  public String toString() {
    return "SubscribedEvent{" +
        "channel=" + channel +
        ", ok=" + result +
        '}';
  }
}
