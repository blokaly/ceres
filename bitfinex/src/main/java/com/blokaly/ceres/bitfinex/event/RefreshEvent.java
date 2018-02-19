package com.blokaly.ceres.bitfinex.event;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import java.util.Collection;
import java.util.Collections;

import static com.blokaly.ceres.data.MarketDataIncremental.Type.DONE;
import static com.blokaly.ceres.data.MarketDataIncremental.Type.UPDATE;

public class RefreshEvent extends ChannelEvent implements MarketDataIncremental<IdBasedOrderInfo> {

    private final JsonArray data;
    private final long sequence;
    private final Type type;
    private final IdBasedOrderInfo orderInfo;

    private static MarketDataIncremental.Type parseType(JsonArray data) {
        if (parseDecimal(data, 2).isZero()) {
            return DONE;
        } else {
            return UPDATE;
        }
    }

    public RefreshEvent(int channelId, long sequence, JsonArray data) {
        super(channelId, "refresh");
        this.data = data;
        this.sequence = sequence;
        this.type = parseType(data);
        this.orderInfo = initOrderInfo();
    }

    private IdBasedOrderInfo initOrderInfo() {
        if (type == UPDATE) {
            return new UpdateOrderInfo();
        } else {
            return new DoneOrderInfo();
        }
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public Collection<IdBasedOrderInfo> orderInfos() {
        return Collections.singletonList(orderInfo);
    }

    @Override
    public String toString() {
        return "RefreshEvent(" + sequence + ")" + orderInfo;
    }

    private static DecimalNumber parseDecimal(JsonArray data, int field) {
        return DecimalNumber.fromStr(data.get(field).getAsString());
    }

    private static OrderInfo.Side parseSide(JsonArray data) {
        double size = data.get(3).getAsDouble();
        if (size == 0) {
            throw new JsonParseException("quantity is zero: " + data);
        }

        return size>0 ? OrderInfo.Side.BUY : OrderInfo.Side.SELL;
    }

    private class MDIncrementalOrderInfo implements IdBasedOrderInfo {
        @Override
        public DecimalNumber getPrice() {
            return parseDecimal(data, 2);
        }

        @Override
        public DecimalNumber getQuantity() {
            return parseDecimal(data, 3).abs();
        }

        @Override
        public String getId() {
            return data.get(1).getAsString();
        }

        @Override
        public Side side() {
            return parseSide(data);
        }
    }

    private class UpdateOrderInfo extends MDIncrementalOrderInfo {
        public String toString() {
            return "[U," + getPrice() + "," + getQuantity() + "]";
        }
    }

    private class DoneOrderInfo extends MDIncrementalOrderInfo {
        public String toString() {
            return "[D," + getId() + "]";
        }
    }
}
