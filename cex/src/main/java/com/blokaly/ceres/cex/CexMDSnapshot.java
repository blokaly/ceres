package com.blokaly.ceres.cex;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.MessageDecoder;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CexMDSnapshot implements MarketDataSnapshot<DepthBasedOrderInfo> {

    private final long sequence;
    private final Collection<DepthBasedOrderInfo> bids;
    private final Collection<DepthBasedOrderInfo> asks;

    private CexMDSnapshot(long sequence, Collection<DepthBasedOrderInfo> bids, Collection<DepthBasedOrderInfo> asks) {
        this.sequence = sequence;
        this.bids = bids;
        this.asks = asks;
    }

    public static CexMDSnapshot parse(MessageDecoder.CexMessage message) {

        if (message.data==null) {
            return new CexMDSnapshot(0, Collections.emptyList(), Collections.emptyList());
        }

        JsonObject jsonObject = message.data;

        long sequence = jsonObject.get("id").getAsLong();

        JsonArray bidArray = jsonObject.get("bids").getAsJsonArray();
        List<DepthBasedOrderInfo> bids = IntStream.range(0, bidArray.size())
                .mapToObj(i->new SnapshotOrderInfo(i, OrderInfo.Side.BUY, bidArray.get(i).getAsJsonArray()))
                .collect(Collectors.toList());

        JsonArray askArray = jsonObject.get("asks").getAsJsonArray();
        List<DepthBasedOrderInfo> asks = IntStream.range(0, askArray.size())
                .mapToObj(i->new SnapshotOrderInfo(i, OrderInfo.Side.SELL, askArray.get(i).getAsJsonArray()))
                .collect(Collectors.toList());

        return new CexMDSnapshot(sequence, bids, asks);
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public Collection<DepthBasedOrderInfo> getBids() {
        return bids;
    }

    @Override
    public Collection<DepthBasedOrderInfo> getAsks() {
        return asks;
    }

    private static class SnapshotOrderInfo implements DepthBasedOrderInfo {
        private final int depth;
        private final Side side;
        private final JsonArray jsonArray;

        private SnapshotOrderInfo(int depth, Side side, JsonArray json) {
            this.depth = depth;
            this.side = side;
            this.jsonArray = json;
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
