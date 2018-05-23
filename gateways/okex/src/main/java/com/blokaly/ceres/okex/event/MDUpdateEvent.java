package com.blokaly.ceres.okex.event;

import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MDUpdateEvent implements ChannelEvent{

  private final String channel;
  private final long sequence;
  private final MarketDataIncremental<OrderInfo> update;
  private final MarketDataIncremental<OrderInfo> deletion;

  @Override
  public EventType getType() {
    return EventType.UPDATE;
  }

  public static MDUpdateEvent parse(String channel, long sequence, JsonArray bidArray, JsonArray askArray) {

    Map<Boolean, List<OrderInfo>> bids = StreamSupport.stream(bidArray.spliterator(), false)
        .map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));

    Map<Boolean, List<OrderInfo>> asks = StreamSupport.stream(askArray.spliterator(), false)
        .map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));

    DepthEvent update = new DepthEvent(sequence, MarketDataIncremental.Type.UPDATE);
    update.add(bids.get(false));
    update.add(asks.get(false));

    DepthEvent deletion = new DepthEvent(sequence, MarketDataIncremental.Type.DONE);
    deletion.add(bids.get(true));
    deletion.add(asks.get(true));

    return new MDUpdateEvent(channel, sequence, update, deletion);
  }

  public MDUpdateEvent(String channel, long sequence, MarketDataIncremental<OrderInfo> update, MarketDataIncremental<OrderInfo> deletion) {
    this.channel = channel;
    this.sequence = sequence;
    this.update = update;
    this.deletion = deletion;
  }

  public String getChannel() {
    return channel;
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
