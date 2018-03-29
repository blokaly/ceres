package com.blokaly.ceres.orderbook;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.proto.OrderBookProto;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class OrderBasedOrderBook implements OrderBook<IdBasedOrderInfo>, TopOfBook {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBasedOrderBook.class);
    private final String symbol;
    private final String key;
    private final NavigableMap<DecimalNumber, PriceLevel> bids = Maps.newTreeMap(Comparator.<DecimalNumber>reverseOrder());
    private final NavigableMap<DecimalNumber, PriceLevel> asks = Maps.newTreeMap();
    private final Map<String, PriceLevel> levelByOrderId = Maps.newHashMap();
    private long lastSequence;

    public OrderBasedOrderBook(String symbol, String key) {
        this.symbol = symbol;
        this.key = key;
        this.lastSequence = 0;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public Collection<? extends Level> getBids() {
        return bids.values();
    }

    @Override
    public Collection<? extends Level> getAsks() {
        return asks.values();
    }

    @Override
    public void clear() {
        bids.clear();
        asks.clear();
        levelByOrderId.clear();
        lastSequence = 0;
    }


    @Override
    public void processSnapshot(MarketDataSnapshot<IdBasedOrderInfo> snapshot) {
        LOGGER.debug("processing snapshot");
        clear();
        snapshot.getBids().forEach(this::processNewOrder);
        snapshot.getAsks().forEach(this::processNewOrder);
        lastSequence = snapshot.getSequence();
    }

    @Override
    public void processIncrementalUpdate(MarketDataIncremental<IdBasedOrderInfo> incremental) {

        if (lastSequence == 0) {
            return;
        }

        long sequence = incremental.getSequence();
        if (sequence <= lastSequence) {
            return;
        }

        LOGGER.debug("processing market data: {}", incremental);
        switch (incremental.type()) {
            case NEW:
                incremental.orderInfos().forEach(this::processNewOrder);
                break;
            case UPDATE:
                incremental.orderInfos().forEach(this::processUpdateOrder);
                break;
            case DONE:
                incremental.orderInfos().forEach(this::processDoneOrder);
                break;
            default:
                LOGGER.debug("Unknown type of market data: {}", incremental);
        }

        lastSequence = sequence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OrderBook{");
        sb.append(symbol).append(", bids=[");
        bids.values().forEach(level -> {sb.append(" ").append(level);});
        sb.append(" ], asks=[");
        asks.values().forEach(level -> {sb.append(" ").append(level);});
        sb.append(" ]}");
        return sb.toString();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Entry topOfBids() {
        Map.Entry<DecimalNumber, PriceLevel> entry = bids.firstEntry();
        return wrapPriceLevel(entry);
    }

    @Override
    public Entry topOfAsks() {
        Map.Entry<DecimalNumber, PriceLevel> entry = asks.firstEntry();
        return wrapPriceLevel(entry);
    }

    private Entry wrapPriceLevel(Map.Entry<DecimalNumber, PriceLevel> entry) {
        if (entry == null) {
            return null;
        } else {
            PriceLevel level = entry.getValue();
            return new Entry(level.getPrice().toString(), level.getQuantity().toString());
        }
    }

    private NavigableMap<DecimalNumber, PriceLevel> sidedLevels(OrderInfo.Side side) {
        if (side == null || side == OrderInfo.Side.UNKNOWN) {
            return null;
        }

        return side== OrderInfo.Side.BUY ? bids : asks;
    }

    private void processNewOrder(IdBasedOrderInfo order) {
        NavigableMap<DecimalNumber, PriceLevel> levels = sidedLevels(order.side());
        DecimalNumber price = order.getPrice();
        String orderId = order.getId();
        PriceLevel level = levels.get(price);
        if (level == null) {
            level = new PriceLevel(price);
            levels.put(price, level);
        }
        level.addOrChange(orderId, order.getQuantity());
        levelByOrderId.put(orderId, level);
    }

    private void processDoneOrder(IdBasedOrderInfo order) {
        String orderId = order.getId();
        PriceLevel level = levelByOrderId.remove(orderId);
        if (level == null) {
            return;
        }

        boolean emptyLevel = level.remove(orderId);
        if (emptyLevel) {
            NavigableMap<DecimalNumber, PriceLevel> levels = sidedLevels(order.side());
            levels.remove(level.getPrice());
        }
    }

    private void processUpdateOrder(IdBasedOrderInfo order) {
        String orderId = order.getId();
        PriceLevel level = levelByOrderId.get(orderId);
        if (level == null) {
            processNewOrder(order);
        } else {
            level.addOrChange(orderId, order.getQuantity());
        }

    }

    private List<OrderBookProto.Level> translateSide(OrderInfo.Side side, int depth) {
        NavigableMap<DecimalNumber, PriceLevel> levels = sidedLevels(side);
        return levels.values().stream().limit(depth).map(level -> {
            OrderBookProto.Level.Builder builder = OrderBookProto.Level.newBuilder();
            builder.setPrice(level.getPrice().toString());
            builder.setSize(level.getQuantity().toString());
            return builder.build();
        }).collect(Collectors.toList());
    }

    public static final class PriceLevel implements OrderBook.Level {

        private final DecimalNumber price;
        private final Map<String, DecimalNumber> quantityByOrderId = Maps.newHashMap();
        private DecimalNumber total;


        private PriceLevel(DecimalNumber price) {
            this.price = price;
            this.total = DecimalNumber.ZERO;
        }

        @Override
        public DecimalNumber getPrice() {
            return price;
        }

        @Override
        public DecimalNumber getQuantity() {
            return total;
        }

        @Override
        public String toString() {
            return "[" + price.toString() + "," + total.toString() + "]";
        }

        private void addOrChange(String orderId, DecimalNumber quantity) {
            total = total.plus(quantity).minus(quantityByOrderId.getOrDefault(orderId, DecimalNumber.ZERO));
            quantityByOrderId.put(orderId, quantity);
        }

        private boolean remove(String orderId) {
            DecimalNumber current = quantityByOrderId.remove(orderId);
            if (current != null) {
                total = total.minus(current);
            }

            if (quantityByOrderId.isEmpty()) {
                return true;
            } else {
                if (total.signum() <= 0) {
                    quantityByOrderId.clear();
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
