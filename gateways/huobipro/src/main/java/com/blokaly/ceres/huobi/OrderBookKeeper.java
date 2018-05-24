package com.blokaly.ceres.huobi;

import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

  private final Map<String, PriceBasedOrderBook> orderbooks;

  @Inject
  public OrderBookKeeper(Map<String, PriceBasedOrderBook> orderbooks) {
    this.orderbooks = orderbooks;
  }

  public PriceBasedOrderBook get(String symbol) {
    return orderbooks.get(symbol);
  }

  public Collection<String> getAllSymbols() {
    return orderbooks.keySet();
  }

  public Collection<PriceBasedOrderBook> getAllBooks() {
    return orderbooks.values();
  }
}
