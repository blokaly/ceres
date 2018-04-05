package com.blokaly.ceres.bitfinex.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SnapshotEvent extends ChannelEvent implements MarketDataSnapshot<IdBasedOrderInfo> {

    private final long sequence;
    private final Collection<IdBasedOrderInfo> bids;
    private final Collection<IdBasedOrderInfo> asks;

    public static SnapshotEvent parse(int channelId, long sequence, JsonArray data) {
        if (data == null || data.size()==0) {
            return new SnapshotEvent(channelId, 0, Collections.emptyList(), Collections.emptyList());
        }

        Map<Boolean, List<IdBasedOrderInfo>> bidAndAsk = StreamSupport.stream(data.spliterator(), false)
                .map(elm -> new SnapshotOrderInfo(elm.getAsJsonArray()))
                .collect(Collectors.partitioningBy(snapshotOrderInfo -> snapshotOrderInfo.side() == OrderInfo.Side.BUY));

        return new SnapshotEvent(channelId, sequence, bidAndAsk.get(true), bidAndAsk.get(false));
    }

    private SnapshotEvent(int channelId, long sequence, Collection<IdBasedOrderInfo> bids, Collection<IdBasedOrderInfo> asks) {
        super(channelId, "snapshot");
        this.sequence = sequence;
        this.bids = bids;
        this.asks = asks;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public Collection<IdBasedOrderInfo> getBids() {
        return bids;
    }

    @Override
    public Collection<IdBasedOrderInfo> getAsks() {
        return asks;
    }

    @Override
    public String toString() {
        return "SnapshotEvent{" +
                "channelId=" + channelId +
                '}';
    }

    private static class SnapshotOrderInfo implements IdBasedOrderInfo {
        private final Side side;
        private final JsonArray jsonArray;

        private SnapshotOrderInfo(JsonArray json) {
            this.jsonArray = json;
            this.side = json.get(2).getAsDouble()>0 ? Side.BUY : Side.SELL;
        }

        @Override
        public String getId() {
            return jsonArray.get(0).getAsString();
        }

        @Override
        public Side side() {
            return side;
        }

        @Override
        public DecimalNumber getPrice() {
            return DecimalNumber.fromStr(jsonArray.get(1).getAsString());
        }

        @Override
        public DecimalNumber getQuantity() {
            return DecimalNumber.fromStr(jsonArray.get(2).getAsString()).abs();
        }
    }
}
