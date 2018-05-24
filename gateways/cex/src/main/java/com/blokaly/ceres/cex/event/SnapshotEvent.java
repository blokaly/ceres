package com.blokaly.ceres.cex.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.common.PairSymbol;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.data.SymbolFormatter;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnapshotEvent extends AbstractEvent implements MarketDataSnapshot<OrderInfo> {

  private final PairSymbol pair;
  private final long sequence;
  private final Collection<OrderInfo> bids;
  private final Collection<OrderInfo> asks;
  private final boolean ok;
  private final String error;

  public static SnapshotEvent fail(String pair, long sequence, String error) {
    return new SnapshotEvent(pair, sequence, false, Collections.emptyList(), Collections.emptyList(), error);
  }

  public static SnapshotEvent parse(long sequence, String pair, JsonArray bidArray, JsonArray askArray) {

    List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray())).collect(Collectors.toList());
    List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray())).collect(Collectors.toList());
    return new SnapshotEvent(pair, sequence, true, bids, asks, null);
  }

  private SnapshotEvent(String pair, long sequence, boolean ok, Collection<OrderInfo> bids, Collection<OrderInfo> asks, String error) {
    super(EventType.SUBSCRIBE.getType());
    String[] syms = pair.split(":");
    this.pair = SymbolFormatter.normalise(syms[0], syms[1]);
    this.sequence = sequence;
    this.ok = ok;
    this.bids = bids;
    this.asks = asks;
    this.error = error;
  }

  public PairSymbol getPair() {
    return pair;
  }

  public boolean isOk() {
    return ok;
  }

  public String getErrorMessage() {
    return error;
  }

  @Override
  public long getSequence() {
    return sequence;
  }

  @Override
  public Collection<OrderInfo> getBids() {
    return bids;
  }

  @Override
  public Collection<OrderInfo> getAsks() {
    return asks;
  }

  private static class SnapshotJsonArrayOrderInfo implements OrderInfo {

    private final Side side;
    private final JsonArray jsonArray;

    private SnapshotJsonArrayOrderInfo(Side side, JsonArray jsonArray) {
      this.side = side;
      this.jsonArray = jsonArray;
    }

    @Override
    public Side side() {
      return side;
    }

    @Override
    public DecimalNumber getPrice() {
      return DecimalNumber.fromStr(jsonArray.get(0).getAsString());
    }

    @Override
    public DecimalNumber getQuantity() {
      return DecimalNumber.fromStr(jsonArray.get(1).getAsString());
    }
  }
}
