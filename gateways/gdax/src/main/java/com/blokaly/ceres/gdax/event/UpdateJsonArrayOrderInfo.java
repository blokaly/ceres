package com.blokaly.ceres.gdax.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

public class UpdateJsonArrayOrderInfo implements OrderInfo {

  private final JsonArray jsonArray;

  public UpdateJsonArrayOrderInfo(JsonArray jsonArray) {
    this.jsonArray = jsonArray;
  }

  @Override
  public Side side() {
    return Side.valueOf(jsonArray.get(0).getAsString().toUpperCase());
  }

  @Override
  public DecimalNumber getPrice() {
    return DecimalNumber.fromStr(jsonArray.get(1).getAsString());
  }

  @Override
  public DecimalNumber getQuantity() {
    return DecimalNumber.fromStr(jsonArray.get(2).getAsString());
  }
}
