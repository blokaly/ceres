package com.blokaly.ceres.bittrex.event;

import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExchangeStateEvent implements MarketDataSnapshot<OrderInfo> {
  private final long sequence;
  private final Collection<OrderInfo> bids;
  private final Collection<OrderInfo> asks;

  public static ExchangeStateEvent parse(long sequence, JsonArray bidArray, JsonArray askArray) {

    List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new JsonOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonObject())).collect(Collectors.toList());
    List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new JsonOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonObject())).collect(Collectors.toList());
    return new ExchangeStateEvent(sequence, bids, asks);
  }

  private ExchangeStateEvent(long sequence, Collection<OrderInfo> bids, Collection<OrderInfo> asks) {
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
    return "OrderBookEvent{" +
        "sequence=" + sequence +
        ", bids=" + bids +
        ", asks=" + asks +
        '}';
  }
}