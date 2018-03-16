package com.blokaly.ceres.gdax.event;

import com.google.gson.Gson;

public class OpenEvent extends AbstractEvent {
  public OpenEvent() {
    super("open");
  }

  public static String jsonString() {
    return new Gson().toJson(new OpenEvent());
  }
}