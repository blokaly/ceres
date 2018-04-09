package com.blokaly.ceres.orderbook;

import com.google.common.base.Objects;

public interface TopOfBook {
  public class Entry {
    public final String price;
    public final String total;
    public final String[] quantities;

    public Entry(String price, String total) {
      this.price = price;
      this.total = total;
      this.quantities = null;
    }

    public Entry(String price, String total, String[] quantities) {
      this.price = price;
      this.total = total;
      this.quantities = quantities;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Entry entry = (Entry) o;
      return Objects.equal(price, entry.price) &&
          Objects.equal(total, entry.total) &&
          Objects.equal(quantities, entry.quantities);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(price, total, quantities);
    }
  }

  String getKey();
  Entry[] topOfBids(int depth);
  Entry[] topOfAsks(int depth);
}
