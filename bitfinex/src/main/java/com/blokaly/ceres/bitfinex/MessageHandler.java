package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.InfoEvent;

public interface MessageHandler {

    void onMessage(InfoEvent event);
}
