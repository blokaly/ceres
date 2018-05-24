package com.blokaly.ceres.huobi;

import com.blokaly.ceres.huobi.event.*;
import com.google.gson.Gson;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonCracker {

  private static Logger LOGGER = LoggerFactory.getLogger(JsonCracker.class);
  private final Gson gson;

  private final MessageHandler messageHandler;

  @Inject
  public JsonCracker(Gson gson, MessageHandler messageHandler) {
    this.gson = gson;
    this.messageHandler = messageHandler;
  }

  public void onOpen() {
    messageHandler.onMessage(new OpenEvent());
  }

  public void onClose() {
    messageHandler.onMessage(new CloseEvent());
  }

  public void crack(String json) {
    WSEvent event = gson.fromJson(json, WSEvent.class);
    LOGGER.debug("event: {}", event);

    EventType type = event.getType();
    if (type == null) {
      return;
    }

    switch (type) {
      case PING:
        messageHandler.onMessage((PingEvent)event);
        break;
      case PONG:
        messageHandler.onMessage((PongEvent)event);
        break;
      case SUBSCRIBE:
        messageHandler.onMessage((SubbedEvent)event);
        break;
      case TICK:
        messageHandler.onMessage((SnapshotEvent)event);
        break;
    }
  }
}
