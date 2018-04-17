package com.blokaly.ceres.data;

public class SymbolFormatter {

  public static String normalise(String symbol) {
    return symbol.replaceAll("[^A-Za-z]", "").toLowerCase();
  }
}
