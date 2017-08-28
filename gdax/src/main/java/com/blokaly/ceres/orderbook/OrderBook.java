package com.blokaly.ceres.orderbook;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.MarketDataSnapshot;

import java.util.Collection;

public interface OrderBook {
    interface Level {
        DecimalNumber getPrice();
        DecimalNumber getQuantity();
    }

    void processSnapshot(MarketDataSnapshot snapshot);
    void processIncrementalUpdate(MarketDataIncremental incremental);
    String getSymbol();
    Collection<? extends Level> getBids();
    Collection<? extends Level> getAsks();
    void clear();
}
