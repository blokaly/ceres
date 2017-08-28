package com.blokaly.ceres.data;

import com.blokaly.ceres.common.DecimalNumber;

public interface OrderInfo {

    enum Side {UNKNOWN, BUY, SELL}

    String getId();

    Side side();

    DecimalNumber getPrice();

    DecimalNumber getQuantity();
}
