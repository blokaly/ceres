package com.blokaly.ceres.kraken;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.common.collect.Lists;
import org.joda.time.DateTimeUtils;
import org.knowm.xchange.kraken.dto.marketdata.KrakenDepth;
import org.knowm.xchange.kraken.dto.marketdata.KrakenPublicOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KrakenSnapshot implements MarketDataSnapshot<DepthBasedOrderInfo> {

  private final long sequece;
  private final Collection<DepthBasedOrderInfo> bids;
  private final Collection<DepthBasedOrderInfo> asks;

  private KrakenSnapshot(long sequece, Collection<DepthBasedOrderInfo> bids, Collection<DepthBasedOrderInfo> asks) {
    this.sequece = sequece;
    this.bids = bids;
    this.asks = asks;
  }

  public static KrakenSnapshot parse(KrakenDepth depth) {

    ArrayList<DepthBasedOrderInfo> bids = getOrderInfoList(depth.getBids(), OrderInfo.Side.BUY);
    ArrayList<DepthBasedOrderInfo> asks = getOrderInfoList(depth.getAsks(), OrderInfo.Side.SELL);
    return new KrakenSnapshot(DateTimeUtils.currentTimeMillis(), bids, asks);
  }

  private static ArrayList<DepthBasedOrderInfo> getOrderInfoList(List<KrakenPublicOrder> bids, OrderInfo.Side side) {
    ArrayList<DepthBasedOrderInfo> infos = Lists.newArrayListWithCapacity(bids.size());
    int idx = 0;
    for (KrakenPublicOrder order : bids) {
      infos.add(new KrakenPublicOrderWrapper(idx++, side, DecimalNumber.fromBD(order.getPrice()), DecimalNumber.fromBD(order.getVolume())));
    }
    return infos;
  }

  @Override
  public long getSequence() {
    return sequece;
  }

  @Override
  public Collection<DepthBasedOrderInfo> getBids() {
    return bids;
  }

  @Override
  public Collection<DepthBasedOrderInfo> getAsks() {
    return asks;
  }

  private static class KrakenPublicOrderWrapper implements DepthBasedOrderInfo {

    private final int depth;
    private final Side side;
    private final DecimalNumber price;
    private final DecimalNumber quantity;

    private KrakenPublicOrderWrapper(int depth, Side side, DecimalNumber price, DecimalNumber quantity) {
      this.depth = depth;
      this.side = side;
      this.price = price;
      this.quantity = quantity;
    }

    @Override
    public int getDepth() {
      return depth;
    }

    @Override
    public Side side() {
      return side;
    }

    @Override
    public DecimalNumber getPrice() {
      return price;
    }

    @Override
    public DecimalNumber getQuantity() {
      return quantity;
    }
  }
}
