package com.blokaly.ceres.huobi.event;

import static com.blokaly.ceres.huobi.event.EventType.SUBSCRIBE;

public class SubEvent implements WSEvent {
  private static final String SUB_STRING = "market.%s.depth.step5";
  private String sub;
  private String id;

  public SubEvent(String symbol) {
    this.id = symbol;
    this.sub = String.format(SUB_STRING, symbol);
  }

  @Override
  public EventType getType() {
    return SUBSCRIBE;
  }

  @Override
  public String toString() {
    return "SubEvent{" +
        "sub=" + sub +
        ", id=" + id +
        '}';
  }
}
