package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.event.SnapshotEvent;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageHandlerImpl implements MessageHandler {

  private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
  private final OrderBookKeeper bookKeeper;
  private final ToBProducer producer;

  @Inject
  public MessageHandlerImpl(OrderBookKeeper bookKeeper, ToBProducer producer) {
    this.bookKeeper = bookKeeper;
    this.producer = producer;
  }

  @Override
  public void onMessage(SnapshotEvent event) {
    LOGGER.debug("snapshot: {}", event);
    PriceBasedOrderBook orderBook = bookKeeper.get(event.getProductId());
    orderBook.processSnapshot(event);
    producer.publish(orderBook);
  }

}
