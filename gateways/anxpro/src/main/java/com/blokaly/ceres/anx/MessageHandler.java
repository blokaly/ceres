package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.event.SnapshotEvent;

public interface MessageHandler {
  void onMessage(SnapshotEvent event);
}
