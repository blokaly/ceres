package com.blokaly.ceres.common;

import java.util.HashMap;
import java.util.Map;

public enum Exchange {

  BEST("BEST", 0),
  FINFABRIK("FFEX", 1),
  GDAX("GDAX", 2),
  BITFINEX("BIFX", 3),
  BITSTAMP("BISP", 4),
  ANXPRO("ANXP", 5)
  ;

  private static final Map<String, Exchange> CODE_MAP = new HashMap<String, Exchange>();
  private static final Map<Integer, Exchange> ID_MAP = new HashMap<Integer, Exchange>();

  static {
    for (Exchange ex : Exchange.values()) {
      CODE_MAP.put(ex.code, ex);
      ID_MAP.put(ex.id, ex);
    }
  }

  private final String code;
  private final int id;

  Exchange(String code, int id) {
    this.code = code;
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public int getId() {
    return id;
  }

  public static Exchange lookupByCode(String code) {
    return CODE_MAP.get(code);
  }

  public static Exchange lookupById(int id) {
    return ID_MAP.get(id);
  }

  public static Exchange parse(String name) {
    for (Exchange exchange : Exchange.values()) {
      if (exchange.name().equals(name)) {
        return exchange;
      }
    }

    return null;
  }
}
