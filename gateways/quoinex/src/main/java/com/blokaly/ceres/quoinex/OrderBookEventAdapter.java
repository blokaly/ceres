package com.blokaly.ceres.quoinex;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.quoinex.OneSidedOrderBookEvent.BuyOrderBookEvent;
import com.google.common.collect.Lists;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.blokaly.ceres.quoinex.OneSidedOrderBookEvent.SellOrderBookEvent;

public abstract class OrderBookEventAdapter {

  private OrderBookEventAdapter(){}

  private static Logger LOGGER = LoggerFactory.getLogger(OrderBookEventAdapter.class);

  public static class BuySideOrderBookEventAdapter implements JsonDeserializer<BuyOrderBookEvent> {
    @Override
    public BuyOrderBookEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      List<DepthBasedOrderInfo> ladders = getOrderInfos(json, OrderInfo.Side.BUY);
      return new BuyOrderBookEvent(System.nanoTime(), ladders);
    }
  }

  public static class SellSideOrderBookEventAdapter implements JsonDeserializer<SellOrderBookEvent> {
    @Override
    public SellOrderBookEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

      List<DepthBasedOrderInfo> ladders = getOrderInfos(json, OrderInfo.Side.SELL);
      return new SellOrderBookEvent(System.nanoTime(),  ladders);
    }
  }

  private static List<DepthBasedOrderInfo> getOrderInfos(JsonElement json, OrderInfo.Side side) {
    JsonArray data = json.getAsJsonArray();
    ArrayList<DepthBasedOrderInfo> infos = Lists.newArrayListWithCapacity(data.size());
    int idx = 0;
    for (JsonElement elm : data) {
      infos.add(new JsonArrayOrderInfo(idx++, side, elm.getAsJsonArray()));
    }
    return infos;
  }

  public static class JsonArrayOrderInfo implements DepthBasedOrderInfo {
    private final int depth;
    private final Side side;
    private final JsonArray jsonArray;

    private JsonArrayOrderInfo(int depth, Side side, JsonArray jsonArray) {
      this.depth = depth;
      this.side = side;
      this.jsonArray = jsonArray;
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
      return DecimalNumber.fromStr(jsonArray.get(0).getAsString());
    }

    @Override
    public DecimalNumber getQuantity() {
      return DecimalNumber.fromStr(jsonArray.get(1).getAsString());
    }
  }
}