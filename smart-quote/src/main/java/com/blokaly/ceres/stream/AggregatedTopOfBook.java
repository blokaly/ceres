package com.blokaly.ceres.stream;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.orderbook.OrderBook;
import com.blokaly.ceres.orderbook.TopOfBook;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AggregatedTopOfBook implements OrderBook<IdBasedOrderInfo>, TopOfBook {

  private static final Logger LOGGER = LoggerFactory.getLogger(AggregatedTopOfBook.class);
  private final String symbol;
  private final String key;
  private final NavigableMap<DecimalNumber, List<OrderInfoLevel>> bids = Maps.newTreeMap(Comparator.<DecimalNumber>reverseOrder());
  private final NavigableMap<DecimalNumber, List<OrderInfoLevel>> asks = Maps.newTreeMap();
  private final Map<String, OrderInfoLevel> bidOrders = Maps.newHashMap();
  private final Map<String, OrderInfoLevel> askOrders = Maps.newHashMap();
  private long lastSequence;

  @Inject
  public AggregatedTopOfBook(String symbol) {
    this.symbol = symbol;
    this.key = symbol + ".top";
    this.lastSequence = 0;
  }

  @Override
  public void processSnapshot(MarketDataSnapshot<IdBasedOrderInfo> snapshot) {
    LOGGER.debug("processing snapshot");
    snapshot.getBids().forEach(this::process);
    snapshot.getAsks().forEach(this::process);
    lastSequence = snapshot.getSequence();
  }

  @Override
  public void processIncrementalUpdate(MarketDataIncremental<IdBasedOrderInfo> incremental) {
    throw new UnsupportedOperationException("MarketDataIncremental not supported");
  }

  @Override
  public String getSymbol() {
    return null;
  }

  @Override
  public Collection<? extends Level> getBids() {
    return null;
  }

  @Override
  public Collection<? extends Level> getAsks() {
    return null;
  }

  @Override
  public void clear() {

  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String[] topOfBids() {
    return format(bids);
  }

  @Override
  public String[] topOfAsks() {
    return format(asks);
  }

  private String[] format(NavigableMap<DecimalNumber, List<OrderInfoLevel>> side) {
    Map.Entry<DecimalNumber, List<OrderInfoLevel>> top = side.firstEntry();
    DecimalNumber total = DecimalNumber.ZERO;
    for (OrderInfoLevel level : top.getValue()) {
      total = total.plus(level.getQuantity());
    }
    return new String[] {top.getKey().toString(), total.toString()};
  }

  public void remove(List<String> staled) {
    for (String id : staled) {
      removeOrder(bidOrders, bids, id);
      removeOrder(askOrders, asks, id);
    }
  }

  private NavigableMap<DecimalNumber, List<OrderInfoLevel>> sidedLevels(OrderInfo.Side side) {
    if (side == null || side == OrderInfo.Side.UNKNOWN) {
      return null;
    }
    return side== OrderInfo.Side.BUY ? bids : asks;
  }

  private Map<String, OrderInfoLevel> sidedOrder(OrderInfo.Side side) {
    if (side == null || side == OrderInfo.Side.UNKNOWN) {
      return null;
    }
    return side== OrderInfo.Side.BUY ? bidOrders : askOrders;
  }

  private void process(IdBasedOrderInfo order) {
    Map<String, OrderInfoLevel> orders = sidedOrder(order.side());
    NavigableMap<DecimalNumber, List<OrderInfoLevel>> levels = sidedLevels(order.side());
    String orderId = order.getId();
    removeOrder(orders, levels, orderId);
    addOrder(order, orders, levels, orderId);
  }

  private void addOrder(IdBasedOrderInfo order, Map<String, OrderInfoLevel> orders, NavigableMap<DecimalNumber, List<OrderInfoLevel>> levels, String orderId) {
    OrderInfoLevel level = new OrderInfoLevel(order);
    DecimalNumber price = level.getPrice();
    orders.put(orderId, level);
    if (levels.containsKey(price)) {
      levels.get(price).add(level);
    } else {
      LinkedList<OrderInfoLevel> group = new LinkedList<>();
      group.add(level);
      levels.put(price, group);
    }
  }

  private void removeOrder(Map<String, OrderInfoLevel> orders, NavigableMap<DecimalNumber, List<OrderInfoLevel>> levels, String orderId) {
    if (orders.containsKey(orderId)) {
      OrderInfoLevel level = orders.remove(orderId);
      DecimalNumber price = level.getPrice();
      List<OrderInfoLevel> group = levels.get(price);
      group.remove(level);
      if (group.isEmpty()) {
        levels.remove(price);
      }
    }
  }

  private class OrderInfoLevel implements Level {
    private final OrderInfo order;

    private OrderInfoLevel(OrderInfo order) {
      this.order = order;
    }

    @Override
    public DecimalNumber getPrice() {
      return order.getPrice();
    }

    @Override
    public DecimalNumber getQuantity() {
      return order.getQuantity();
    }
  }
}
