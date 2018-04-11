package com.blokaly.ceres.kraken;

import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

  private final Map<String, DepthBasedOrderBook> orderbooks;

  @Inject
  public OrderBookKeeper(Map<String, DepthBasedOrderBook> orderbooks) {
    this.orderbooks = orderbooks;
  }

  public DepthBasedOrderBook get(String symbol) {
    return orderbooks.get(symbol);
  }

  public Collection<String> getAllSymbols() {
    return orderbooks.keySet();
  }

  public Collection<DepthBasedOrderBook> getAllBooks() {
    return orderbooks.values();
  }
}

