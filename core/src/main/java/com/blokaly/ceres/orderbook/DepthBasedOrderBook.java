package com.blokaly.ceres.orderbook;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.proto.OrderBookProto;
import com.lmax.disruptor.EventTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DepthBasedOrderBook implements OrderBook<DepthBasedOrderInfo>, EventTranslator<OrderBookProto.OrderBookMessage.Builder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBasedOrderBook.class);
    private final String symbol;
    private final int depth;
    private final PriceLevel[] bids;
    private final PriceLevel[] asks;
    private long lastSequence;

    public DepthBasedOrderBook(String symbol, int depth) {
        this.symbol = symbol;
        this.lastSequence = 0;
        this.depth = depth;
        bids = new PriceLevel[depth];
        asks = new PriceLevel[depth];
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Collection<? extends Level> getBids() {
        return Arrays.asList(bids);
    }

    @Override
    public Collection<? extends Level> getAsks() {
        return Arrays.asList(asks);
    }

    @Override
    public void clear() {
        lastSequence = 0;
        for (int i = 0; i < bids.length; i++) {
            bids[i] = null;
            asks[i] = null;
        }
    }

    @Override
    public void processSnapshot(MarketDataSnapshot<DepthBasedOrderInfo> snapshot) {
        LOGGER.debug("processing snapshot");
        clear();
        processNewOrder(snapshot.getBids(), sidedLevels(OrderInfo.Side.BUY));
        processNewOrder(snapshot.getAsks(), sidedLevels(OrderInfo.Side.SELL));
        lastSequence = snapshot.getSequence();
    }

    @Override
    public void processIncrementalUpdate(MarketDataIncremental<DepthBasedOrderInfo> incremental) {
        if (lastSequence == 0) {
            initSnapshot();
            return;
        }

        long sequence = incremental.getSequence();
        if (sequence <= lastSequence) {
            return;
        }

        incremental.orderInfos().forEach(this::processUpdateOrder);
        lastSequence = sequence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OrderBook{");
        sb.append(symbol).append(", bids=[");
        for (PriceLevel bid : bids) {
            sb.append(" ").append(bid);
        }
        sb.append(" ], asks=[");
        for (PriceLevel ask : asks) {
            sb.append(" ").append(ask);
        }
        sb.append(" ]}");
        return sb.toString();
    }

    protected void initSnapshot() { }


    private void processUpdateOrder(DepthBasedOrderInfo order) {
        PriceLevel[] levels = sidedLevels(order.side());
        if (order.getDepth() >= levels.length) {
            return;
        }
        levels[order.getDepth()].update(order.getPrice(), order.getQuantity());
    }

    private PriceLevel[] sidedLevels(OrderInfo.Side side) {
        if (side == null || side == OrderInfo.Side.UNKNOWN) {
            return null;
        }

        return side== OrderInfo.Side.BUY ? bids : asks;
    }

    private void processNewOrder(Collection<DepthBasedOrderInfo> levels, PriceLevel[] side) {

        for (DepthBasedOrderInfo level : levels) {
            if (level.getDepth() >= side.length) {
                return;
            }
            DecimalNumber price = level.getPrice();
            DecimalNumber quantity = level.getQuantity();
            side[level.getDepth()] = new PriceLevel(price, quantity);
        }
    }

    @Override
    public void translateTo(OrderBookProto.OrderBookMessage.Builder event, long sequence) {
        event.clear();
        event.setSymbol(symbol);
        event.addAllBids(translateSide(OrderInfo.Side.BUY));
        event.addAllAsks(translateSide(OrderInfo.Side.SELL));
    }

    private List<OrderBookProto.Level> translateSide(OrderInfo.Side side) {
        PriceLevel[] levels = sidedLevels(side);
        return Arrays.stream(levels).map(level -> {
            OrderBookProto.Level.Builder builder = OrderBookProto.Level.newBuilder();
            builder.setPrice(level.getPrice().toString());
            builder.setSize(level.getQuantity().toString());
            return builder.build();
        }).collect(Collectors.toList());
    }

    private final class PriceLevel implements OrderBook.Level {

        private DecimalNumber price;
        private DecimalNumber total;


        private PriceLevel(DecimalNumber price, DecimalNumber quantity) {
            this.price = price;
            this.total = quantity;
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

        private void update(DecimalNumber price, DecimalNumber quantity) {
            if (!price.isZero()) {
                this.price = price;
            }
            if (!quantity.isZero()) {
                this.total = quantity;
            }
        }

    }
}
