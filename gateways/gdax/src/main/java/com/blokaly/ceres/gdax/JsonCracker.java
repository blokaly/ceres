package com.blokaly.ceres.gdax;

import com.blokaly.ceres.gdax.event.*;
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

    EventType type = EventType.get(event.getEvent());
    if (type == null) {
      return;
    }

    switch (type) {
      case HB:
        messageHandler.onMessage((HbEvent)event);
        break;
      case SUBS:
        messageHandler.onMessage((SubscribedEvent)event);
        break;
      case SNAPSHOT:
        messageHandler.onMessage((SnapshotEvent)event);
        break;
      case L2U:
        messageHandler.onMessage((L2UpdateEvent)event);
        break;
    }
  }
}
