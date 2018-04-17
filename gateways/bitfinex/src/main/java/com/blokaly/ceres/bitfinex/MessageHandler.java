package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.*;

public interface MessageHandler {

    void onMessage(HbEvent event);

    void onMessage(InfoEvent event);

    void onMessage(SnapshotEvent event);

    void onMessage(RefreshEvent event);

    void onMessage(SubscribedEvent event);

    void onMessage(PingEvent event);

    void onMessage(PongEvent event);

    void onMessage(ErrorEvent event);
}
