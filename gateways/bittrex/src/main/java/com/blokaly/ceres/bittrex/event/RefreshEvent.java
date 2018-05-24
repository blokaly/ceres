package com.blokaly.ceres.bittrex.event;

import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RefreshEvent implements MarketDataIncremental<OrderInfo> {

    private final long sequence;
    private final Type type;
    private final List<OrderInfo> orderInfoList;

    public RefreshEvent(long sequence, Type type) {
        this.sequence = sequence;
        this.type = type;
        orderInfoList = new ArrayList<>();
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    public void add(JsonOrderInfo orderInfo) {
        orderInfoList.add(orderInfo);
    }
    
    @Override
    public Collection<OrderInfo> orderInfos() {
        return orderInfoList;
    }

    public void add(List<JsonOrderInfo> orders) {
        orderInfoList.addAll(orders);
    }

    @Override
    public String toString() {
        return "RefreshEvent{" +
                "sequence=" + sequence +
                ", type=" + type +
                '}';
    }
}
