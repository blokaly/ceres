package com.blokaly.ceres.bittrex.event;

import com.blokaly.ceres.bittrex.JsonKey;
import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static com.blokaly.ceres.bittrex.JsonKey.Quantity;
import static com.blokaly.ceres.bittrex.JsonKey.Rate;

public class JsonOrderInfo implements OrderInfo {

  public enum Type {
    ADD, UPDATE, REMOVE;
    private static final Map<Integer, Type> lookup = new HashMap<Integer, Type>();
    static {
      lookup.put(0, ADD);
      lookup.put(1, UPDATE);
      lookup.put(2, REMOVE);
    }
    public static Type get(int type) {
      return lookup.get(type);
    }
  }

  private final Side side;
  private final JsonObject obj;

  public JsonOrderInfo(Side side, JsonObject obj) {
    this.side = side;
    this.obj = obj;
  }

  public Type getType() {
    if (obj.has(JsonKey.Type.getKey())) {
      return Type.get(obj.get(JsonKey.Type.getKey()).getAsInt());
    } else {
      return null;
    }
  }

  @Override
  public Side side() {
    return side;
  }

  @Override
  public DecimalNumber getPrice() {
    return DecimalNumber.fromStr(obj.get(Rate.getKey()).getAsString());
  }

  @Override
  public DecimalNumber getQuantity() {
    DecimalNumber quantity = DecimalNumber.fromStr(obj.get(Quantity.getKey()).getAsString());
    return getType()==Type.REMOVE ? quantity.negate() : quantity;
  }
}
