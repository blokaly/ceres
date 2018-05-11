package com.blokaly.ceres.binance.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

public class JsonArrayOrderInfo implements OrderInfo {

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
