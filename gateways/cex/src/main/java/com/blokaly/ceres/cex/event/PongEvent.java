package com.blokaly.ceres.cex.event;

public class PongEvent extends AbstractEvent {

  public PongEvent(){
    super(EventType.PONG.getType());
  }
}
