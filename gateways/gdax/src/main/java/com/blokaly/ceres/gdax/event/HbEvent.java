package com.blokaly.ceres.gdax.event;

public class HbEvent extends ChannelEvent {

  private long sequence;
  private long last_trade_id;
  private String time;

  public HbEvent(String productId) {
    super("heartbeat", productId);
  }

  @Override
  public String toString() {
    return "HbEvent{" +
        "sequence=" + sequence +
        ", product=" + product_id +
        '}';
  }
}
