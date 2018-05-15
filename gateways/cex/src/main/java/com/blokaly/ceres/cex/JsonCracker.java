package com.blokaly.ceres.cex;

import com.blokaly.ceres.cex.event.*;
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
    AbstractEvent event = gson.fromJson(json, AbstractEvent.class);
    LOGGER.debug("event: {}", event);

    EventType type = event.getType();
    if (type == null) {
      return;
    }

    switch (type) {
      case CONNECTED:
        messageHandler.onMessage((ConnectedEvent) event);
        break;
      case AUTH:
        messageHandler.onMessage((AuthEvent) event);
        break;
      case SUBSCRIBE:
        messageHandler.onMessage((SnapshotEvent) event);
        break;
      case UPDATE:
        messageHandler.onMessage((MDUpdateEvent) event);
        break;
      case PING:
        messageHandler.onMessage((PingEvent) event);
        break;
    }
  }
}
