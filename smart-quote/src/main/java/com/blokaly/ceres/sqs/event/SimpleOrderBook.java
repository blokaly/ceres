package com.blokaly.ceres.sqs.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SimpleOrderBook implements MarketDataSnapshot<OrderInfo> {

  private String symbol;
  private long sequence;
  private final Collection<OrderInfo> bids;
  private final Collection<OrderInfo> asks;

  public static SimpleOrderBook parse(JsonArray bidArray, JsonArray askArray) {

    List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray())).collect(Collectors.toList());
    List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray())).collect(Collectors.toList());
    return new SimpleOrderBook(bids, asks);
  }

  private SimpleOrderBook(Collection<OrderInfo> bids, Collection<OrderInfo> asks) {
    this.bids = bids;
    this.asks = asks;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public void setSequence(long sequence) {
    this.sequence = sequence;
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
    return "SimpleOrderBook{" +
        "symbol=" + symbol +
        ", sequence=" + sequence +
        ", bids=" + bids +
        ", asks=" + asks +
        '}';
  }

  private static class JsonArrayOrderInfo implements OrderInfo {

    private final Side side;
    private final JsonArray jsonArray;

    private JsonArrayOrderInfo(Side side, JsonArray jsonArray) {
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

    @Override
    public String toString() {
      return jsonArray.toString();
    }
  }
}
