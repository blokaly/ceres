package com.blokaly.ceres.quote;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonOrderBook implements MarketDataSnapshot<IdBasedOrderInfo> {

  private String symbol;
  private long sequence;
  private final Collection<IdBasedOrderInfo> bids;
  private final Collection<IdBasedOrderInfo> asks;

  public static JsonOrderBook parse(String id, JsonArray bid, JsonArray ask) {

    List<IdBasedOrderInfo> bids = bid==null ? Collections.emptyList() : Collections.singletonList(new JsonArrayOrderInfo(id, OrderInfo.Side.BUY, bid.getAsJsonArray()));
    List<IdBasedOrderInfo> asks = ask==null ? Collections.emptyList() : Collections.singletonList(new JsonArrayOrderInfo(id, OrderInfo.Side.SELL, ask.getAsJsonArray()));
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
      if (jsonArray.size() > 0) {
        return DecimalNumber.fromStr(jsonArray.get(0).getAsString());
      } else {
        return null;
      }
    }

    @Override
    public DecimalNumber getQuantity() {
      if (jsonArray.size() > 0) {
        return DecimalNumber.fromStr(jsonArray.get(1).getAsString());
      } else {
        return null;
      }
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
