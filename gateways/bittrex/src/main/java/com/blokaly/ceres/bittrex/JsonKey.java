package com.blokaly.ceres.bittrex;

public enum JsonKey {

  Nonce("N"), Buys("Z"), Sells("S"), Fills("f"),
  MarketName("M"), Type("TY"),Quantity("Q"), Rate("R");

  private final String key;
  private JsonKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
