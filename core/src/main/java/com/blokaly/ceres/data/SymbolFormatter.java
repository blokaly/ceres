package com.blokaly.ceres.data;

import com.blokaly.ceres.common.PairSymbol;

public class SymbolFormatter {

  public static String normalise(String symbol) {
    return symbol.replaceAll("[^A-Za-z]", "").toLowerCase();
  }

  public static PairSymbol normalise(String base, String terms) {
    return new PairSymbol(normalise(base), normalise(terms));
  }
}
