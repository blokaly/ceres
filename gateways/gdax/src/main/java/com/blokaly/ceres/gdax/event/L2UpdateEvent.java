package com.blokaly.ceres.gdax.event;

import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class L2UpdateEvent extends ChannelEvent {

  private final long sequence;
  private final MarketDataIncremental<OrderInfo> update;
  private final MarketDataIncremental<OrderInfo> deletion;

  public static L2UpdateEvent parse(long sequence, String productId, JsonArray updates) {

    Map<Boolean, List<OrderInfo>> updatedOrders = StreamSupport.stream(updates.spliterator(), false)
        .map(elm -> new UpdateJsonArrayOrderInfo(elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));


    RefreshEvent update = new RefreshEvent(sequence, MarketDataIncremental.Type.UPDATE);
    update.add(updatedOrders.get(false));

    RefreshEvent deletion = new RefreshEvent(sequence, MarketDataIncremental.Type.DONE);
    deletion.add(updatedOrders.get(true));

    return new L2UpdateEvent(sequence, productId, update, deletion);
  }

  private L2UpdateEvent(long sequence, String productId, MarketDataIncremental<OrderInfo> update, MarketDataIncremental<OrderInfo> deletion) {
    super("l2update", productId);
    this.sequence = sequence;
    this.update = update;
    this.deletion = deletion;
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
