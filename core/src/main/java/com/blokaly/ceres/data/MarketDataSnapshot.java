package com.blokaly.ceres.data;

import java.util.Collection;

public interface MarketDataSnapshot<T extends OrderInfo> {

    long getSequence();

    Collection<T> getBids();

    Collection<T> getAsks();
}
