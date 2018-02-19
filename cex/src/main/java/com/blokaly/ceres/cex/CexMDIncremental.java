package com.blokaly.ceres.cex;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.*;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.blokaly.ceres.data.MarketDataIncremental.Type.UPDATE;

public class CexMDIncremental implements MarketDataIncremental<DepthBasedOrderInfo> {

    private final JsonObject message;
    private Collection<DepthBasedOrderInfo> orderInfos;
    private final long sequence;
    private final Type type;

    private CexMDIncremental(JsonObject message, long sequence, Collection<DepthBasedOrderInfo> orderInfos) {
        this.message = message;
        this.orderInfos = orderInfos;
        this.type = UPDATE;
        this.sequence = sequence;
    }

    public static CexMDIncremental parse(MessageDecoder.CexMessage message) {
        long sequence = message.data.get("id").getAsLong();

        JsonArray bidArray = message.data.getAsJsonArray("bids");
        ArrayList<DepthBasedOrderInfo> orderInfos = new ArrayList<>();
        if (bidArray.size() > 0) {
            orderInfos.addAll(IntStream.range(0, bidArray.size())
                    .mapToObj(i->new MDIncrementalOrderInfo(i, OrderInfo.Side.BUY, bidArray.get(i).getAsJsonArray()))
                    .collect(Collectors.toList()));
        }

        JsonArray askArray = message.data.getAsJsonArray("asks");
        if (askArray.size() > 0) {
            orderInfos.addAll(IntStream.range(0, askArray.size())
                    .mapToObj(i->new MDIncrementalOrderInfo(i, OrderInfo.Side.SELL, askArray.get(i).getAsJsonArray()))
                    .collect(Collectors.toList()));
        }
        return new CexMDIncremental(message.data, sequence, orderInfos);
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public MarketDataIncremental.Type type() {
        return type;
    }

    @Override
    public Collection<DepthBasedOrderInfo> orderInfos() {
        return orderInfos;
    }

    public String toString() {
        return message.toString();
    }


    private static class MDIncrementalOrderInfo implements DepthBasedOrderInfo {
        private final int depth;
        private final Side side;
        private final JsonArray jsonArray;

        private MDIncrementalOrderInfo(int depth, Side side, JsonArray json) {
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
