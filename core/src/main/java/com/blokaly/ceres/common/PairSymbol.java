package com.blokaly.ceres.common;

import com.blokaly.ceres.data.SymbolFormatter;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class PairSymbol {
  private final String base;
  private final String terms;

  public static PairSymbol parse(String pair, String delimiter) {
    String[] syms = pair.split(delimiter);
    return SymbolFormatter.normalise(syms[0], syms[1]);
  }

  public PairSymbol(@Nonnull String base, @Nonnull String terms) {
    this.base = base;
    this.terms = terms;
  }

  public PairSymbol invert() {
    return new PairSymbol(this.terms, this.base);
  }

  public String getBase() {
    return base;
  }

  public String getTerms() {
    return terms;
  }

  public String getCode() {
    return base + terms;
  }

  public String toString(String delimiter) {
    if (delimiter == null) {
      return getCode();
    }  else {
      return base + delimiter + terms;
    }
  }
  public String toString() {
    return getCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PairSymbol that = (PairSymbol) o;
    return Objects.equal(base, that.base) &&
        Objects.equal(terms, that.terms);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(base, terms);
  }
}
