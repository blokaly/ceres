package com.blokaly.ceres.orderbook;

import com.blokaly.ceres.gdax.GdaxMDSnapshot;
import com.blokaly.ceres.web.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdaxOrderBook extends OrderBasedOrderBook {
    private static final Logger LOGGER = LoggerFactory.getLogger(GdaxOrderBook.class);

    public GdaxOrderBook(String symbol) {
        super(symbol);
    }

    @Override
    protected void initSnapshot() {
        try {
            GdaxMDSnapshot snapshot = RestClient.orderBookSnapshot(getSymbol());
            processSnapshot(snapshot);
        } catch (Exception e) {
            LOGGER.error("Failed to get/process snapshot", e);
        }
    }
}
