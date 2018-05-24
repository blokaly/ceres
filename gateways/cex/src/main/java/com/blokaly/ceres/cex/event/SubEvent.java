package com.blokaly.ceres.cex.event;

import com.blokaly.ceres.common.PairSymbol;

import java.util.Arrays;

public class SubEvent extends AbstractEvent {

  private String oid;
  private Data data;

  public SubEvent(String oid) {
    super(EventType.SUBSCRIBE.getType());
    this.oid = oid;
  }

  public void sub(PairSymbol pair) {
    data = new Data(pair.getBase().toUpperCase(), pair.getTerms().toUpperCase());
  }

  public static class Data {
    private String[] pair;
    private boolean subscribe = true;
    private long depth = 0L;

    private Data(String base, String terms) {
      pair = new String[]{base, terms};
    }

    @Override
    public String toString() {
      return "{" +
          "pair=" + Arrays.toString(pair) +
          ", depth=" + depth +
          '}';
    }
  }

  @Override
  public String toString() {
    return "SubEvent{" +
        "oid=" + oid +
        ", data=" + data +
        ", e=" + e +
        '}';
  }
}
