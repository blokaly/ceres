package com.blokaly.ceres.okcoin;

import com.blokaly.ceres.okcoin.event.*;

public interface MessageHandler {
  void onMessage(OpenEvent event);
  void onMessage(CloseEvent closeEvent);
  void onMessage(SubscribedEvent event);
  void onMessage(MDUpdateEvent event);
  void onMessage(ErrorEvent event);
}
