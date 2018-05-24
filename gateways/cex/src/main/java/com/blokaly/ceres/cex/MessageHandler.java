package com.blokaly.ceres.cex;

import com.blokaly.ceres.cex.event.*;

public interface MessageHandler {
  void onMessage(OpenEvent event);
  void onMessage(CloseEvent event);
  void onMessage(ConnectedEvent event);
  void onMessage(AuthEvent event);
  void onMessage(SnapshotEvent event);
  void onMessage(MDUpdateEvent event);
  void onMessage(PingEvent event);
}
