package com.blokaly.ceres.gdax.event;

import java.util.Arrays;

public abstract class AbstractEvent {

  public String type;

  AbstractEvent() {}

  AbstractEvent(String event) {
    this.type = event;
  }

  public String getEvent() {
    return type;
  }

  public static class Channel {
    private String name;
    private String[] product_ids;

    @Override
    public String toString() {
      return "{" +
          "name=" + name +
          ", product_ids=" + Arrays.toString(product_ids) +
          '}';
    }
  }
}