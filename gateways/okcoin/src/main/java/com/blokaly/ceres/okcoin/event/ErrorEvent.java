package com.blokaly.ceres.okcoin.event;

public class ErrorEvent implements ChannelEvent {
  private boolean result;
  private String error_msg;
  private int error_code;

  @Override
  public EventType getType() {
    return EventType.ERROR;
  }

  @Override
  public String toString() {
    return "ErrorEvent{" +
        "result=" + result +
        ", msg=" + error_msg +
        ", code=" + error_code +
        '}';
  }
}
