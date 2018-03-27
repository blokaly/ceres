package com.blokaly.ceres.orderbook;

public interface TopOfBook {
  String getKey();
  String[] topOfBids();
  String[] topOfAsks();
}
