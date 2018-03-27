package com.blokaly.ceres.stream;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonOrderBook implements MarketDataSnapshot<IdBasedOrderInfo> {

  private String symbol;
  private long sequence;
  private final Collection<IdBasedOrderInfo> bids;
  private final Collection<IdBasedOrderInfo> asks;

  public static JsonOrderBook parse(String id, JsonArray bidArray, JsonArray askArray) {

    List<IdBasedOrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false).map(elm -> new JsonArrayOrderInfo(id, OrderInfo.Side.BUY, elm.getAsJsonArray())).collect(Collectors.toList());
    List<IdBasedOrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false).map(elm -> new JsonArrayOrderInfo(id, OrderInfo.Side.SELL, elm.getAsJsonArray())).collect(Collectors.toList());
    return new JsonOrderBook(bids, asks);
  }

  private JsonOrderBook(Collection<IdBasedOrderInfo> bids, Collection<IdBasedOrderInfo> asks) {
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
  public Collection<IdBasedOrderInfo> getBids() {
    return bids;
  }

  @Override
  public Collection<IdBasedOrderInfo> getAsks() {
    return asks;
  }

  @Override
  public String toString() {
    return "JsonOrderBook{" +
        "sym=" + symbol +
        ", seq=" + sequence +
        ", bids=" + bids +
        ", asks=" + asks +
        '}';
  }

  private static class JsonArrayOrderInfo implements IdBasedOrderInfo {

    private final String id;
    private final Side side;
    private final JsonArray jsonArray;

    private JsonArrayOrderInfo(String id, Side side, JsonArray jsonArray) {
      this.id = id;
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

    @Override
    public String getId() {
      return id;
    }
  }
}
