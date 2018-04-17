package com.blokaly.ceres.quote;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.common.Source;
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

public class BestTopOfBook implements OrderBook<IdBasedOrderInfo>, TopOfBook {

  private static final Logger LOGGER = LoggerFactory.getLogger(BestTopOfBook.class);
  private static final String SUFFIX = "." + Source.BEST.getCode();
  private final String symbol;
  private final String key;
  private final NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> bids = Maps.newTreeMap(Comparator.<DecimalNumber>reverseOrder());
  private final NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> asks = Maps.newTreeMap();
  private final Map<String, IdBasedOrderInfo> bidOrders = Maps.newHashMap();
  private final Map<String, IdBasedOrderInfo> askOrders = Maps.newHashMap();
  private long lastSequence;

  @Inject
  public BestTopOfBook(String symbol) {
    this.symbol = symbol;
    this.key = symbol + SUFFIX;
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
    bids.clear();
    bidOrders.clear();
    asks.clear();
    askOrders.clear();
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public Entry[] topOfBids(int depth) {
    if (depth != 1) {
      throw new IllegalArgumentException("Only support level 1");
    }
    return new Entry[]{format(bids)};
  }

  @Override
  public Entry[] topOfAsks(int depth) {
    if (depth != 1) {
      throw new IllegalArgumentException("Only support level 1");
    }
    return new Entry[]{format(asks)};
  }

  private Entry format(NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> side) {
    Map.Entry<DecimalNumber, List<IdBasedOrderInfo>> top = side.firstEntry();
    if (top == null) {
      return null;
    }
    DecimalNumber total = DecimalNumber.ZERO;
    List<IdBasedOrderInfo> quantities = top.getValue();
    String[] details = new String[quantities.size()];
    int idx = 0;
    for (IdBasedOrderInfo level : quantities) {
      DecimalNumber quantity = level.getQuantity();
      total = total.plus(quantity);
      Source source = Source.lookupByCode(level.getId());
      String exId = source == null ? level.getId() : String.valueOf(source.getId());
      details[idx++] = exId + ":" + quantity.toString();
    }
    return new Entry(top.getKey().toString(), total.toString(), details);
  }

  public void remove(List<String> staled) {
    for (String id : staled) {
      removeOrder(bidOrders, bids, id);
      removeOrder(askOrders, asks, id);
    }
  }

  private NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> sidedLevels(OrderInfo.Side side) {
    if (side == null || side == OrderInfo.Side.UNKNOWN) {
      return null;
    }
    return side== OrderInfo.Side.BUY ? bids : asks;
  }

  private Map<String, IdBasedOrderInfo> sidedOrder(OrderInfo.Side side) {
    if (side == null || side == OrderInfo.Side.UNKNOWN) {
      return null;
    }
    return side== OrderInfo.Side.BUY ? bidOrders : askOrders;
  }

  private void process(IdBasedOrderInfo order) {
    Map<String, IdBasedOrderInfo> orders = sidedOrder(order.side());
    NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> levels = sidedLevels(order.side());
    String orderId = order.getId();
    removeOrder(orders, levels, orderId);
    addOrder(order, orders, levels, orderId);
  }

  private void addOrder(IdBasedOrderInfo order, Map<String, IdBasedOrderInfo> orders, NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> levels, String orderId) {
    DecimalNumber price = order.getPrice();
    if (price==null || price.isZero()) {
      return;
    }
    orders.put(orderId, order);
    if (levels.containsKey(price)) {
      levels.get(price).add(order);
    } else {
      LinkedList<IdBasedOrderInfo> group = new LinkedList<>();
      group.add(order);
      levels.put(price, group);
    }
  }

  private void removeOrder(Map<String, IdBasedOrderInfo> orders, NavigableMap<DecimalNumber, List<IdBasedOrderInfo>> levels, String orderId) {
    if (orders.containsKey(orderId)) {
      IdBasedOrderInfo level = orders.remove(orderId);
      DecimalNumber price = level.getPrice();
      List<IdBasedOrderInfo> group = levels.get(price);
      group.remove(level);
      if (group.isEmpty()) {
        levels.remove(price);
      }
    }
  }
}
