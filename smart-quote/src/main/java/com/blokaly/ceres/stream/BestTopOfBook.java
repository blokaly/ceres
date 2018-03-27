package com.blokaly.ceres.stream;

import com.blokaly.ceres.common.DecimalNumber;
import com.blokaly.ceres.data.IdBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataIncremental;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.data.OrderInfo;
import com.blokaly.ceres.orderbook.OrderBook;
import com.blokaly.ceres.orderbook.TopOfBook;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BestTopOfBook implements OrderBook<IdBasedOrderInfo>, TopOfBook {

  private static final Logger LOGGER = LoggerFactory.getLogger(BestTopOfBook.class);
  private final String symbol;
  private final String key;
  private final PriceLevel bid;
  private final PriceLevel ask;
  private long lastSequence;

  @Inject
  public BestTopOfBook(String symbol) {
    this.symbol = symbol;
    this.key = symbol + ".top";
    this.lastSequence = 0;
    this.bid = new PriceLevel(DecimalNumber.ZERO, DecimalNumber::compareTo);
    this.ask = new PriceLevel(DecimalNumber.MAX, Comparator.reverseOrder());
  }

  @Override
  public void processSnapshot(MarketDataSnapshot<IdBasedOrderInfo> snapshot) {
    LOGGER.debug("processing snapshot");
    processOrders(OrderInfo.Side.BUY, snapshot.getBids());
    processOrders(OrderInfo.Side.SELL, snapshot.getAsks());
    lastSequence = snapshot.getSequence();
  }

  private void processOrders(OrderInfo.Side side, Collection<IdBasedOrderInfo> updates) {

    IdBasedOrderInfo order = updates.iterator().next();
    if (order == null) {
      return;
    }

    if ( side == OrderInfo.Side.BUY ) {
      bid.update(order);
    } else {
      ask.update(order);
    }
  }

  @Override
  public void processIncrementalUpdate(MarketDataIncremental<IdBasedOrderInfo> incremental) {
    throw new UnsupportedOperationException("MarketDataIncremental not supported");
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public Collection<? extends Level> getBids() {
    return Collections.singletonList(bid);
  }

  @Override
  public Collection<? extends Level> getAsks() {
    return Collections.singletonList(ask);
  }

  @Override
  public void clear() {
    lastSequence = 0;
    bid.reset();
    ask.reset();
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String[] topOfBids() {
    return wrapPriceLevel(bid);
  }

  @Override
  public String[] topOfAsks() {
    return wrapPriceLevel(ask);
  }

  private String[] wrapPriceLevel(PriceLevel level) {
    return new String[]{level.getPrice().toString(), level.getQuantity().toString()};
  }

  public static final class PriceLevel implements Level {

    private final Map<String, DecimalNumber> quantityByOrderId = Maps.newHashMap();
    private final Comparator<DecimalNumber> comparator;
    private DecimalNumber price;
    private DecimalNumber total;

    public PriceLevel(DecimalNumber initial, Comparator<DecimalNumber> comparator) {
      this.comparator = comparator;
      this.price = initial;
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

    private void reset() {
      price = DecimalNumber.ZERO;
      total = DecimalNumber.ZERO;
      quantityByOrderId.clear();

    }
    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PriceLevel that = (PriceLevel) o;
      return valueEquals(price, that.price) && valueEquals(total, that.total);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(price, total);
    }

    private boolean valueEquals(DecimalNumber v1, DecimalNumber v2) {
      return v1 == v2 || v1 != null && v1.compareTo(v2)==0;
    }

    private void update(IdBasedOrderInfo order) {
      DecimalNumber price = order.getPrice();
      DecimalNumber quantity = order.getQuantity();
      String ordId = order.getId();
      int result = comparator.compare(this.price, price);
      if (result<0) {
        this.price = price;
        total = quantity;
        quantityByOrderId.clear();
        quantityByOrderId.put(ordId, quantity);
      } else if (result == 0) {
        total = total.plus(quantity).minus(quantityByOrderId.getOrDefault(ordId, DecimalNumber.ZERO));
        quantityByOrderId.put(ordId, quantity);
      }
    }
  }
}
