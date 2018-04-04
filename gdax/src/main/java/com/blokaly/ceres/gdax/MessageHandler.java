package com.blokaly.ceres.gdax;

import com.blokaly.ceres.gdax.event.*;

public interface MessageHandler {
  void onMessage(OpenEvent event);
  void onMessage(CloseEvent closeEvent);
  void onMessage(SubscribedEvent event);
  void onMessage(SnapshotEvent event);
  void onMessage(L2UpdateEvent event);
  void onMessage(HbEvent event);
}
