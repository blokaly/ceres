package com.blokaly.ceres.cex.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.common.PairSymbol;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.data.SymbolFormatter;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MDUpdateEvent extends AbstractEvent {

  private final PairSymbol pair;
  private final long sequence;
  private final MarketDataIncremental<OrderInfo> update;
  private final MarketDataIncremental<OrderInfo> deletion;

  public static MDUpdateEvent parse(long sequence, String pair, JsonArray bidArray, JsonArray askArray) {

    Map<Boolean, List<OrderInfo>> bids = StreamSupport.stream(bidArray.spliterator(), false)
        .map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));

    Map<Boolean, List<OrderInfo>> asks = StreamSupport.stream(askArray.spliterator(), false)
        .map(elm -> new JsonArrayOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray()))
        .collect(Collectors.partitioningBy(order -> order.getQuantity().isZero()));

    RefreshEvent update = new RefreshEvent(sequence, MarketDataIncremental.Type.UPDATE);
    update.add(bids.get(false));
    update.add(asks.get(false));

    RefreshEvent deletion = new RefreshEvent(sequence, MarketDataIncremental.Type.DONE);
    deletion.add(bids.get(true));
    deletion.add(asks.get(true));

    return new MDUpdateEvent(pair, sequence, update, deletion);
  }

  private MDUpdateEvent(String pair, long sequence, RefreshEvent update, RefreshEvent deletion) {
    super(EventType.UPDATE.getType());
    String[] syms = pair.split(":");
    this.pair = SymbolFormatter.normalise(syms[0], syms[1]);
    this.sequence = sequence;
    this.update = update;
    this.deletion = deletion;
  }

  public PairSymbol getPair() {
    return pair;
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

  public static class JsonArrayOrderInfo implements OrderInfo {

    private final Side side;
    private final JsonArray jsonArray;

    public JsonArrayOrderInfo(Side side, JsonArray jsonArray) {
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
  }
}
