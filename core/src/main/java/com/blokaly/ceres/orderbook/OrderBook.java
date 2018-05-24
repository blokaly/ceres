package com.blokaly.ceres.orderbook;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;

import java.util.Collection;

public interface OrderBook<T extends OrderInfo> {
    interface Level {
        DecimalNumber getPrice();
        DecimalNumber getQuantity();
    }

    void processSnapshot(MarketDataSnapshot<T> snapshot);
    void processIncrementalUpdate(MarketDataIncremental<T> incremental);
    String getSymbol();
    long getLastSequence();
    Collection<? extends Level> getBids();
    Collection<? extends Level> getAsks();
    void clear();
}
