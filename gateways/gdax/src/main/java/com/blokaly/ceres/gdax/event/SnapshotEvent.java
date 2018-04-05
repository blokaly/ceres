package com.blokaly.ceres.gdax.event;

import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnapshotEvent extends ChannelEvent implements MarketDataSnapshot<OrderInfo> {

  private final long sequence;
  private final Collection<OrderInfo> bids;
  private final Collection<OrderInfo> asks;

  public static SnapshotEvent parse(long sequence, String productId, JsonArray bidArray, JsonArray askArray) {

    List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray())).collect(Collectors.toList());
    List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray())).collect(Collectors.toList());
    return new SnapshotEvent(productId, sequence, bids, asks);
  }

  private SnapshotEvent(String productId, long sequence, Collection<OrderInfo> bids, Collection<OrderInfo> asks) {
    super("snapshot", productId);
    this.sequence = sequence;
    this.bids = bids;
    this.asks = asks;
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
        ", symbol=" + getProductId() +
        ", bids=" + bids +
        ", asks=" + asks +
        '}';
  }
}
