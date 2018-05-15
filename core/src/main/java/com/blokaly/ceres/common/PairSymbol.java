package com.blokaly.ceres.common;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class PairSymbol {
  private final String base;
  private final String terms;

  public PairSymbol(@Nonnull String base, @Nonnull String terms) {
    this.base = base;
    this.terms = terms;
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
