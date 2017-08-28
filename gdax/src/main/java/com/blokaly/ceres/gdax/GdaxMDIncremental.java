package com.blokaly.ceres.gdax;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.data.OrderInfo.Side;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.blokaly.ceres.data.MarketDataIncremental.Type.*;

public class GdaxMDIncremental implements MarketDataIncremental {

    private static final String ID_FIELD = "order_id";
    private static final String TYPE_FIELD = "type";
    private static final String PRICE_FIELD = "price";
    private static final String SIZE_FIELD = "size";
    private static final String REMAIN_SIZE_FIELD = "remaining_size";
    private static final String NEW_SIZE_FIELD = "new_size";
    private static final String SIDE_FIELD = "side";
    private static final String SEQUENCE_FIELD = "sequence";

    private final static JsonParser JSON_PARSER = new JsonParser();
    private final JsonObject message;
    private final long sequence;
    private final Type type;
    private final OrderInfo orderInfo;

    private GdaxMDIncremental(JsonObject message, long sequence) {
        this.message = message;
        this.type = parseType(message);
        this.sequence = sequence;
        this.orderInfo = initOrderInfo();
    }

    private OrderInfo initOrderInfo() {
        switch (type) {
            case NEW: return new NewOrderInfo();
            case DONE: return new DoneOrderInfo();
            case UPDATE: return new UpdateOrderInfo();
            default: return null;
        }
    }

    public static GdaxMDIncremental parse(String jsonString) {
        JsonObject jsonObject = JSON_PARSER.parse(jsonString).getAsJsonObject();
        long sequence = jsonObject.get(SEQUENCE_FIELD).getAsLong();
        return new GdaxMDIncremental(jsonObject, sequence);
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
    public OrderInfo orderInfo() {
        return orderInfo;
    }

    public String toString() {
        return message.toString();
    }

    public static MarketDataIncremental.Type parseType(JsonObject message) {
        String type = message.get(TYPE_FIELD).getAsString();
        if (Strings.isNullOrEmpty(type)) {
            return UNKNOWN;
        }

        switch (type) {
            case "open": return NEW;
            case "done": return DONE;
            case "change":
            {
                if (message.has(NEW_SIZE_FIELD)) {
                    return UPDATE;
                } else {
                    return UNKNOWN;
                }
            }
            default: return UNKNOWN;
        }
    }

    public static DecimalNumber parseDecimal(JsonObject message, String field) {
        return DecimalNumber.fromStr(message.get(field).getAsString());
    }

    public static Side parseSide(JsonObject message) {
        String sideVal = message.get(SIDE_FIELD).getAsString();
        if (Strings.isNullOrEmpty(sideVal)) {
            return Side.UNKNOWN;
        }

        if ("buy".equals(sideVal)) {
            return Side.BUY;
        } else if ("sell".equals(sideVal)) {
            return Side.SELL;
        } else {
            return Side.UNKNOWN;
        }
    }

    private class MDIncrementalOrderInfo implements OrderInfo {
        @Override
        public DecimalNumber getPrice() {
            return parseDecimal(message, PRICE_FIELD);
        }

        @Override
        public DecimalNumber getQuantity() {
            return parseDecimal(message, REMAIN_SIZE_FIELD);
        }

        @Override
        public String getId() {
            return message.get(ID_FIELD).getAsString();
        }

        @Override
        public Side side() {
            return parseSide(message);
        }
    }

    private class NewOrderInfo extends MDIncrementalOrderInfo {
        public String toString() {
            return "[N," + getPrice() + "," + getQuantity() + "]";
        }
    }

    private class DoneOrderInfo extends MDIncrementalOrderInfo {
        public String toString() {
            return "[D," + getPrice() + "," + getQuantity() + "]";
        }
    }

    private class UpdateOrderInfo extends MDIncrementalOrderInfo {
        @Override
        public DecimalNumber getQuantity() {
            return parseDecimal(message, NEW_SIZE_FIELD);
        }

        public String toString() {
            return "[U," + getPrice() + "," + getQuantity() + "]";
        }
    }
}
