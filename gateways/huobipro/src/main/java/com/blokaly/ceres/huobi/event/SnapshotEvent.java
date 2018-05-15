package com.blokaly.ceres.huobi.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnapshotEvent implements WSEvent, MarketDataSnapshot<OrderInfo> {

  private final String symbol;
  private final long sequence;
  private final Collection<OrderInfo> bids;
  private final Collection<OrderInfo> asks;

  public static SnapshotEvent parse(long sequence, String symbol, JsonArray bidArray, JsonArray askArray) {

    List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray())).collect(Collectors.toList());
    List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray())).collect(Collectors.toList());
    return new SnapshotEvent(symbol, sequence, bids, asks);
  }

  private SnapshotEvent(String symbol, long sequence, Collection<OrderInfo> bids, Collection<OrderInfo> asks) {
    this.symbol = symbol;
    this.sequence = sequence;
    this.bids = bids;
    this.asks = asks;
  }

  @Override
  public EventType getType() {
    return EventType.TICK;
  }

  public String getSymbol() {
    return symbol;
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

  @Override
  public String toString() {
    return "SnapshotEvent{" +
        ", symbol=" + symbol +
        ", bids=" + bids +
        ", asks=" + asks +
        '}';
  }

  private static class SnapshotJsonArrayOrderInfo implements OrderInfo {

    private final Side side;
    private final JsonArray jsonArray;

    public SnapshotJsonArrayOrderInfo(Side side, JsonArray jsonArray) {
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
