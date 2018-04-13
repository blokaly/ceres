package com.blokaly.ceres.coinmarketcap;

import com.blokaly.ceres.common.DecimalNumber;

public class TickerEvent {
    private final String symbol;
    private final DecimalNumber usdPrice;
    private final long lastUpdate;

  public TickerEvent(String symbol, DecimalNumber usdPrice, long lastUpdate) {
    this.symbol = symbol;
    this.usdPrice = usdPrice;
    this.lastUpdate = lastUpdate;
  }

  public boolean isValid() {
    return !usdPrice.isZero();
  }
}
