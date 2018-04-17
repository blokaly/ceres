package com.blokaly.ceres.common;

import com.google.common.base.Objects;

import java.io.Serializable;

public class Pair<L, R> implements Serializable {

  public static final Pair<?, ?> NULL_PAIR = new Pair<>(null, null);

  private final L left;
  private final R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equal(left, pair.left) &&
        Objects.equal(right, pair.right);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(left, right);
  }

  @Override
  public String toString() {
    return "Pair{" + left + ", " + right + '}';
  }
}
