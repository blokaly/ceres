package com.blokaly.ceres.quoinex;

import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

public class OrderBookHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderBookHandler.class);
  private final DepthBasedOrderBook orderBook;
  private final ToBProducer producer;

  public OrderBookHandler(DepthBasedOrderBook orderBook, ToBProducer producer) {
    this.orderBook = orderBook;
    this.producer = producer;
  }

  public String getSymbol() {
    return orderBook.getSymbol();
  }

  public void handle(OneSidedOrderBookEvent event) {
    orderBook.processIncrementalUpdate(event);
    producer.publish(orderBook);
  }

  public void init() {
    orderBook.processSnapshot(new EmptySnapshot());
  }

  private static class EmptySnapshot implements MarketDataSnapshot<DepthBasedOrderInfo> {
    @Override
    public long getSequence() {
      return System.nanoTime();
    }

    @Override
    public Collection<DepthBasedOrderInfo> getBids() {
      return Collections.emptyList();
    }

    @Override
    public Collection<DepthBasedOrderInfo> getAsks() {
      return Collections.emptyList();
    }
  }
}
