package com.blokaly.ceres.bitstamp.event;

import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DiffBookEvent {

    private final long sequence;
    private final MarketDataIncremental<OrderInfo> update;
    private final MarketDataIncremental<OrderInfo> deletion;

    public static DiffBookEvent parse(long sequence, JsonArray bidArray, JsonArray askArray) {

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

        return new DiffBookEvent(sequence, update, deletion);
    }

    public DiffBookEvent(long sequence, MarketDataIncremental<OrderInfo> update, MarketDataIncremental<OrderInfo> deletion) {
        this.sequence = sequence;
        this.update = update;
        this.deletion = deletion;
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
}
