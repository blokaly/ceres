package com.blokaly.ceres.gdax.event;

public abstract class ChannelEvent extends AbstractEvent {

  public final String product_id;

  public ChannelEvent(String type, String product_id) {
    super(type);
    this.product_id = product_id;
  }

  public String getProductId() {
    return product_id;
  }
}