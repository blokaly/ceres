package com.blokaly.ceres.cex;

import com.blokaly.ceres.common.PairSymbol;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

  private final Map<PairSymbol, PriceBasedOrderBook> orderbooks;

  @Inject
  public OrderBookKeeper(Map<PairSymbol, PriceBasedOrderBook> orderbooks) {
    this.orderbooks = orderbooks;
  }

  public PriceBasedOrderBook get(PairSymbol pair) {
    return orderbooks.get(pair);
  }

  public Collection<PairSymbol> getAllPairs() {
    return orderbooks.keySet();
  }

  public Collection<PriceBasedOrderBook> getAllBooks() {
    return orderbooks.values();
  }
}
