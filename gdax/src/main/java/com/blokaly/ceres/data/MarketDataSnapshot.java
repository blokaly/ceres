package com.blokaly.ceres.data;

import java.util.Collection;

public interface MarketDataSnapshot {

    long getSequence();

    Collection<OrderInfo> getBids();

    Collection<OrderInfo> getAsks();
}
