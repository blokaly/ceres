package com.blokaly.ceres.okex;

import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.okex.event.*;
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
  private final Provider<OKExClient> clientProvider;
  private final OrderBookKeeper bookKeeper;
  private final ToBProducer producer;

  @Inject
  public MessageHandlerImpl(Gson gson, Provider<OKExClient> clientProvider, OrderBookKeeper bookKeeper, ToBProducer producer) {
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
  public void onMessage(SubscribedEvent event) {
    if (event.isOk()) {
      LOGGER.info("{}", event);
      bookKeeper.get(event.getChannel()).clear();
    } else {
      LOGGER.error("{}", event);
    }
  }

  @Override
  public void onMessage(MDUpdateEvent event) {
    LOGGER.debug("{}", event);
    PriceBasedOrderBook orderBook = bookKeeper.get(event.getChannel());
    orderBook.processIncrementalUpdate(event.getDeletion());
    orderBook.processIncrementalUpdate(event.getUpdate());
    producer.publish(orderBook);
  }

  @Override
  public void onMessage(ErrorEvent event) {
    LOGGER.error("{}", event);
  }

  private void subscribeAll() {
    OKExClient sender = clientProvider.get();
    if (sender == null) {
      LOGGER.error("OKCoin client unavailable, skip subscription");
      return;
    }
    bookKeeper.getAllChannels().forEach(chan -> {
      String subscription = gson.toJson(new SubEvent(chan));
      LOGGER.info("subscribe: {}", subscription);
      sender.send(subscription);
    });
  }

}
