package com.blokaly.ceres.bitfinex.event;

public class ErrorEvent extends AbstractEvent {
    private String msg;
    private String code;

  @Override
  public String toString() {
    return "ErrorEvent{" +
        "msg=" + msg +
        ", code=" + code +
        '}';
  }
}
