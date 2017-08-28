package com.blokaly.ceres.gdax;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GdaxMDSnapshot implements MarketDataSnapshot {

    private final static JsonParser JSON_PARSER = new JsonParser();
    private final long sequence;
    private final Collection<OrderInfo> bids;
    private final Collection<OrderInfo> asks;

    private GdaxMDSnapshot(long sequence, Collection<OrderInfo> bids, Collection<OrderInfo> asks) {
        this.sequence = sequence;
        this.bids = bids;
        this.asks = asks;
    }

    public static GdaxMDSnapshot parse(String jsonString) {

        if (jsonString == null || jsonString.isEmpty()) {
            return new GdaxMDSnapshot(0, Collections.emptyList(), Collections.emptyList());
        }

        JsonObject jsonObject = JSON_PARSER.parse(jsonString).getAsJsonObject();

        long sequence = jsonObject.get("sequence").getAsLong();

        JsonArray bidArray = jsonObject.get("bids").getAsJsonArray();
        List<OrderInfo> bids = StreamSupport.stream(bidArray.spliterator(), false)
                .map(elm -> new SnapshotOrderInfo(OrderInfo.Side.BUY, elm.getAsJsonArray()))
                .collect(Collectors.toList());

        JsonArray askArray = jsonObject.get("asks").getAsJsonArray();
        List<OrderInfo> asks = StreamSupport.stream(askArray.spliterator(), false)
                .map(elm -> new SnapshotOrderInfo(OrderInfo.Side.SELL, elm.getAsJsonArray()))
                .collect(Collectors.toList());

        return new GdaxMDSnapshot(sequence, bids, asks);
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public Collection<OrderInfo> getBids() {
        return bids;
    }

    @Override
    public Collection<OrderInfo> getAsks() {
        return asks;
    }

    private static class SnapshotOrderInfo implements OrderInfo {
        private final Side side;
        private final JsonArray jsonArray;

        private SnapshotOrderInfo(Side side, JsonArray json) {
            this.side = side;
            this.jsonArray = json;
        }

        @Override
        public String getId() {
            return jsonArray.get(2).getAsString();
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
