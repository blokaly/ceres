package com.blokaly.ceres.gdax.event;

import java.util.Collection;

public class OrderBookEvent extends AbstractEvent {

  private final String[] channels = {"level2", "heartbeat"};
  private final String[] product_ids;

  public OrderBookEvent(Collection<String> symbols) {
    super("subscribe");
    this.product_ids = symbols.toArray(new String[symbols.size()]);
  }
}
