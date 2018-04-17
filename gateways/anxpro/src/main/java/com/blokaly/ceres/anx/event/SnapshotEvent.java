package com.blokaly.ceres.anx.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnapshotEvent extends AbstractEvent implements MarketDataSnapshot<OrderInfo> {

  private final long sequence;
  public final String productId;
  private final Collection<OrderInfo> bids;
  private final Collection<OrderInfo> asks;

  public static SnapshotEvent parse(long sequence, String productId, JsonArray bidArray, JsonArray askArray) {

    List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonObject())).collect(Collectors.toList());
    List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new SnapshotJsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonObject())).collect(Collectors.toList());
    return new SnapshotEvent(sequence, productId, bids, asks);
  }

  private SnapshotEvent(long sequence, String productId, Collection<OrderInfo> bids, Collection<OrderInfo> asks) {
    super("orderBook");
    this.sequence = sequence;
    this.productId = productId;
    this.bids = bids;
    this.asks = asks;
  }

  @Override
  public long getSequence() {
    return sequence;
  }

  public String getProductId() {
    return productId;
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
    private final JsonObject jsonObject;

    private SnapshotJsonArrayOrderInfo(Side side, JsonObject jsonObject) {
      this.side = side;
      this.jsonObject = jsonObject;
    }

    @Override
    public Side side() {
      return side;
    }

    @Override
    public DecimalNumber getPrice() {
      return DecimalNumber.fromStr(jsonObject.get("price").getAsString());
    }

    @Override
    public DecimalNumber getQuantity() {
      return DecimalNumber.fromStr(jsonObject.get("quantity").getAsString());
    }
  }
}
