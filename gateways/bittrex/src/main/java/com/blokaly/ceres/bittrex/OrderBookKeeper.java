package com.blokaly.ceres.bittrex;

import com.blokaly.ceres.common.PairSymbol;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

  private final Map<PairSymbol, OrderBookHandler> orderbooks;

  @Inject
  public OrderBookKeeper(Map<PairSymbol, OrderBookHandler> orderbooks) {
    this.orderbooks = orderbooks;
  }

  public OrderBookHandler get(PairSymbol pair) {
    return orderbooks.get(pair);
  }

  public Collection<PairSymbol> getAllPairs() {
    return orderbooks.keySet();
  }

  public void init() {
    orderbooks.values().forEach(OrderBookHandler::start);
  }
}
