package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.InfoEvent;
import com.blokaly.ceres.bitfinex.event.RefreshEvent;
import com.blokaly.ceres.bitfinex.event.SnapshotEvent;
import com.blokaly.ceres.bitfinex.event.SubscribedEvent;

public interface MessageHandler {

    void onMessage(InfoEvent event);

    void onMessage(SnapshotEvent event);

    void onMessage(RefreshEvent event);

    void onMessage(SubscribedEvent event);
}
