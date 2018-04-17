package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.event.AbstractEvent;
import com.blokaly.ceres.anx.event.EventType;
import com.blokaly.ceres.anx.event.SnapshotEvent;
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

  public void crack(String json) {
    AbstractEvent event = gson.fromJson(json, AbstractEvent.class);
    LOGGER.debug("event: {}", event);

    EventType type = EventType.get(event.getEvent());
    if (type == null) {
      return;
    }

    switch (type) {
      case SNAPSHOT:
        messageHandler.onMessage((SnapshotEvent)event);
        break;
    }
  }
}
