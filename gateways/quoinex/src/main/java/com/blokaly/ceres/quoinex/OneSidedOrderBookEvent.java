package com.blokaly.ceres.quoinex;

import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;

import java.util.Collection;

public class OneSidedOrderBookEvent implements MarketDataIncremental<DepthBasedOrderInfo> {
  private final long sequence;
  private final Collection<DepthBasedOrderInfo> ladders;
  private final OrderInfo.Side side;


  static class BuyOrderBookEvent extends OneSidedOrderBookEvent {
    BuyOrderBookEvent(long sequence, Collection<DepthBasedOrderInfo> ladders) {
      super(sequence, OrderInfo.Side.BUY, ladders);
    }
  }

  static class SellOrderBookEvent extends OneSidedOrderBookEvent {
    SellOrderBookEvent(long sequence, Collection<DepthBasedOrderInfo> ladders) {
      super(sequence, OrderInfo.Side.SELL, ladders);
    }
  }

  protected OneSidedOrderBookEvent(long sequence, OrderInfo.Side side, Collection<DepthBasedOrderInfo> ladders) {
    this.sequence = sequence;
    this.side = side;
    this.ladders = ladders;
  }

  @Override
  public long getSequence() {
    return sequence;
  }

  @Override
  public Type type() {
    return Type.NEW;
  }

  @Override
  public Collection<DepthBasedOrderInfo> orderInfos() {
    return ladders;
  }

  @Override
  public String toString() {
    return "OneSidedOrderBookEvent{" +
        "sequence=" + sequence +
        ", side=" + side +
        ", ladders=" + ladders +
        '}';
  }
}