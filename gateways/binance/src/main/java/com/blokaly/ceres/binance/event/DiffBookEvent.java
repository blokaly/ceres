package com.blokaly.ceres.binance.event;

import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DiffBookEvent {

  private final long begin;
  private final long end;
  private final MarketDataIncremental<OrderInfo> update;
  private final MarketDataIncremental<OrderInfo> deletion;

  public static DiffBookEvent parse(long begin, long end, JsonArray bidArray, JsonArray askArray) {

    Map<Boolean, List<OrderInfo>> bids = StreamSupport.stream(bidArray.spliterator(), false)
        .map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));

    Map<Boolean, List<OrderInfo>> asks = StreamSupport.stream(askArray.spliterator(), false)
        .map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));

    RefreshEvent update = new RefreshEvent(end, MarketDataIncremental.Type.UPDATE);
    update.add(bids.get(false));
    update.add(asks.get(false));

    RefreshEvent deletion = new RefreshEvent(end, MarketDataIncremental.Type.DONE);
    deletion.add(bids.get(true));
    deletion.add(asks.get(true));

    return new DiffBookEvent(begin, end, update, deletion);
  }

  public DiffBookEvent(long begin, long end, MarketDataIncremental<OrderInfo> update, MarketDataIncremental<OrderInfo> deletion) {
    this.begin = begin;
    this.end = end;
    this.update = update;
    this.deletion = deletion;
  }

  public long getBeginSequence() {
    return begin;
  }

  public long getEndSequence() {
    return end;
  }

  public MarketDataIncremental<OrderInfo> getUpdate() {
    return update;
  }

  public MarketDataIncremental<OrderInfo> getDeletion() {
    return deletion;
  }
}
