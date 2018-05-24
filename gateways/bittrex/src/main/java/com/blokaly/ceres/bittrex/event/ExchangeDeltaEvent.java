package com.blokaly.ceres.bittrex.event;

import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExchangeDeltaEvent {
  private final String market;
  private final long sequence;
  private final MarketDataIncremental<OrderInfo> update;
  private final MarketDataIncremental<OrderInfo> deletion;

  public static ExchangeDeltaEvent parse(String market, long sequence, JsonArray bidArray, JsonArray askArray) {

    Map<Boolean, List<JsonOrderInfo>> bids = StreamSupport.stream(bidArray.spliterator(), false)
        .map(elm -> new JsonOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonObject()))
        .collect(Collectors.partitioningBy(ExchangeDeltaEvent::isRemove));

    Map<Boolean, List<JsonOrderInfo>> asks = StreamSupport.stream(askArray.spliterator(), false)
        .map(elm -> new JsonOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonObject()))
        .collect(Collectors.partitioningBy(ExchangeDeltaEvent::isRemove));

    RefreshEvent update = new RefreshEvent(sequence, MarketDataIncremental.Type.UPDATE);
    update.add(bids.get(false));
    update.add(asks.get(false));

    RefreshEvent deletion = new RefreshEvent(sequence, MarketDataIncremental.Type.DONE);
    deletion.add(bids.get(true));
    deletion.add(asks.get(true));

    return new ExchangeDeltaEvent(market, sequence, update, deletion);
  }

  private static boolean isRemove(JsonOrderInfo orderInfo) {
//    return orderInfo.getQuantity().isZero() || (orderInfo.getType() != null && orderInfo.getType() == JsonOrderInfo.Type.REMOVE);
    return orderInfo.getQuantity().isZero();
  }

  private ExchangeDeltaEvent(String market, long sequence, MarketDataIncremental<OrderInfo> update, MarketDataIncremental<OrderInfo> deletion) {
    this.market = market;
    this.sequence = sequence;
    this.update = update;
    this.deletion = deletion;
  }

  public String getMarket() {
    return market;
  }

  public long getSequence() {
    return sequence;
  }

  public MarketDataIncremental<OrderInfo> getUpdate() {
    return update;
  }

  public MarketDataIncremental<OrderInfo> getDeletion() {
    return deletion;
  }
}
