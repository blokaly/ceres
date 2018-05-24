package com.blokaly.ceres.common;

import com.typesafe.config.Config;
import com.typesafe.config.impl.ConfigImplUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum Source {

  BEST("BEST", 0),
  FINFABRIK("FFEX", 1),
  GDAX("GDAX", 2),
  BITFINEX("BIFX", 3),
  BITSTAMP("BISP", 4),
  ANXPRO("ANXP", 5),
  KRAKEN("KRAK", 6),
  COINMARKETCAP("CMCP", 7),
  BINANCE("BIAN", 8),
  QUOINEX("QUIN", 9),
  HUOBIPRO("HOBI", 10),
  CEX("CEXO", 11),
  BITTREX("BTRX", 12),
  OKCOIN("OKCN", 13),
  OKEX("OKEX", 14)
  ;

  private static final Map<String, Source> CODE_MAP = new HashMap<String, Source>();
  private static final Map<Integer, Source> ID_MAP = new HashMap<Integer, Source>();

  static {
    for (Source ex : Source.values()) {
      CODE_MAP.put(ex.code, ex);
      ID_MAP.put(ex.id, ex);
    }
  }

  private final String code;
  private final int id;

  Source(String code, int id) {
    this.code = code;
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public int getId() {
    return id;
  }

  public static Source lookupByCode(String code) {
    return CODE_MAP.get(code);
  }

  public static Source lookupById(int id) {
    return ID_MAP.get(id);
  }

  public static Source parse(String name) {
    for (Source source : Source.values()) {
      if (source.name().equals(name)) {
        return source;
      }
    }

    return null;
  }

  @Nullable
  public static String getCode(Config config, String path) {
    try {
      return Source.valueOf(
          Configs.getOrDefault(config, path, Configs.STRING_EXTRACTOR, "").toUpperCase())
          .getCode();
    } catch (Exception ex) {
      return null;
    }
  }
}
