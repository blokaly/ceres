package com.blokaly.ceres.orderbook;

import com.blokaly.ceres.gdax.GdaxMDSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdaxOrderBook extends OrderBasedOrderBook {
    private static final Logger LOGGER = LoggerFactory.getLogger(GdaxOrderBook.class);

    public GdaxOrderBook(String symbol) {
        super(symbol);
    }

}
