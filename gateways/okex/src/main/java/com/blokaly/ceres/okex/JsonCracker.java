package com.blokaly.ceres.okex;

import com.blokaly.ceres.okex.event.*;
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
    ChannelEvent event = gson.fromJson(json, ChannelEvent.class);
    LOGGER.debug("event: {}", event);
    EventType type = event.getType();
    if (type == null || type == EventType.NOOP) {
      return;
    }

    switch (type) {
      case SUBSCRIBED:
        messageHandler.onMessage((SubscribedEvent)event);
        break;
      case UPDATE:
        messageHandler.onMessage((MDUpdateEvent)event);
        break;
      case ERROR:
        messageHandler.onMessage((ErrorEvent)event);
        break;
    }
  }

}
