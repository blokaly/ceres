package com.blokaly.ceres.gdax;

import com.blokaly.ceres.gdax.event.*;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandlerImpl implements MessageHandler {

  private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
  private final Gson gson;
  private final MessageSender sender;
  private final OrderBookKeeper bookKeeper;
  private final ToBProducer producer;

  public MessageHandlerImpl(Gson gson, MessageSender sender, OrderBookKeeper bookKeeper, ToBProducer producer) {
    this.gson = gson;
    this.sender = sender;
    this.bookKeeper = bookKeeper;
    this.producer = producer;
  }

  @Override
  public void onMessage(OpenEvent event) {
    String jsonString = gson.toJson(new OrderBookEvent(bookKeeper.getAllSymbols()));
    LOGGER.info("subscribing: {}", jsonString);
    sender.send(jsonString);
  }

  @Override
  public void onMessage(HbEvent event) {
    LOGGER.debug("hb: {}", event);
  }

  @Override
  public void onMessage(SubscribedEvent event) {
    LOGGER.info("subscribed: {}", event);
  }

  @Override
  public void onMessage(SnapshotEvent event) {
    LOGGER.debug("snapshot: {}", event);
    PriceBasedOrderBook orderBook = bookKeeper.get(event.getProductId());
    orderBook.processSnapshot(event);
  }

  @Override
  public void onMessage(L2UpdateEvent event) {
    LOGGER.debug("l2update: {}", event);
    PriceBasedOrderBook orderBook = bookKeeper.get(event.getProductId());
    orderBook.processIncrementalUpdate(event.getDeletion());
    orderBook.processIncrementalUpdate(event.getUpdate());
    producer.publish(orderBook);
  }
}
