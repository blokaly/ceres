package com.blokaly.ceres.huobi.event;

import static com.blokaly.ceres.huobi.event.EventType.SUBSCRIBED;

public class SubbedEvent implements WSEvent {
  private String id;
  private String subbed;
  private long ts;
  private String status;

  @Override
  public EventType getType() {
    return SUBSCRIBED;
  }

  @Override
  public String toString() {
    return "SubbedEvent{" +
        "id=" + id +
        ", subbed=" + subbed +
        ", ts=" + ts +
        ", status=" + status +
        '}';
  }
}
