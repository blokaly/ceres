package com.blokaly.ceres.huobi;

import com.blokaly.ceres.huobi.event.*;

public interface MessageHandler {
  void onMessage(OpenEvent event);
  void onMessage(CloseEvent closeEvent);
  void onMessage(PingEvent event);
  void onMessage(PongEvent event);
  void onMessage(SubbedEvent event);
  void onMessage(SnapshotEvent event);
}
