package com.blokaly.ceres.huobi;

import com.blokaly.ceres.huobi.event.*;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageHandlerImpl implements MessageHandler {

  private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
  private final Gson gson;
  private final Provider<HuobiClient> clientProvider;
  private final OrderBookKeeper bookKeeper;
  private final ToBProducer producer;

  @Inject
  public MessageHandlerImpl(Gson gson, Provider<HuobiClient> clientProvider, OrderBookKeeper bookKeeper, ToBProducer producer) {
    this.gson = gson;
    this.clientProvider = clientProvider;
    this.bookKeeper = bookKeeper;
    this.producer = producer;
  }

  @Override
  public void onMessage(OpenEvent event) {
    LOGGER.info("Open and subscribing all symbols...");
    subscribeAll();
  }

  @Override
  public void onMessage(CloseEvent closeEvent) {
    LOGGER.info("publishing stale status due to close");
    bookKeeper.getAllBooks().forEach(book -> {
      book.clear();
      producer.publish(book);
    });
  }

  @Override
  public void onMessage(PingEvent event) {
    LOGGER.info("{}", event);
    clientProvider.get().send(gson.toJson(new PongEvent(event.getPing())));
  }

  @Override
  public void onMessage(PongEvent event) {
    LOGGER.info("{}", event);
  }

  @Override
  public void onMessage(SubbedEvent event) {
    LOGGER.info("subbed received: {}", event);
  }

  private void subscribeAll() {
    HuobiClient sender = clientProvider.get();
    if (sender == null) {
      LOGGER.error("Huobi client unavailable, skip subscription");
      return;
    }
    bookKeeper.getAllSymbols().forEach(symbol -> {
      String subscription = gson.toJson(new SubEvent(symbol));
      LOGGER.info("subscribe: {}", subscription);
      sender.send(subscription);
    });
  }

  @Override
  public void onMessage(SnapshotEvent event) {
    LOGGER.debug("snapshot: {}", event);
    PriceBasedOrderBook orderBook = bookKeeper.get(event.getSymbol());
    orderBook.processSnapshot(event);
    producer.publish(orderBook);
  }
}
