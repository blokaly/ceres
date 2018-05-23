package com.blokaly.ceres.okex;

import com.blokaly.ceres.okex.event.*;

public interface MessageHandler {
  void onMessage(OpenEvent event);
  void onMessage(CloseEvent closeEvent);
  void onMessage(SubscribedEvent event);
  void onMessage(MDUpdateEvent event);
  void onMessage(ErrorEvent event);
}
