package com.blokaly.ceres.cex;

import com.blokaly.ceres.cex.event.*;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.common.PairSymbol;
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
  private final Provider<CexClient> clientProvider;
  private final OrderBookKeeper bookKeeper;
  private final ToBProducer producer;
  private final PongEvent pong = new PongEvent();

  @Inject
  public MessageHandlerImpl(Gson gson, Provider<CexClient> clientProvider, OrderBookKeeper bookKeeper, ToBProducer producer) {
    this.gson = gson;
    this.clientProvider = clientProvider;
    this.bookKeeper = bookKeeper;
    this.producer = producer;
  }

  @Override
  public void onMessage(OpenEvent event) {
    LOGGER.info("WebSocket opened");
  }

  @Override
  public void onMessage(CloseEvent closeEvent) {
    LOGGER.info("WebSocket closed, publishing stale status...");
    bookKeeper.getAllBooks().forEach(book -> {
      book.clear();
      producer.publish(book);
    });
  }

  @Override
  public void onMessage(ConnectedEvent event) {
    LOGGER.info("WebSocket connected");
    long timestamp = System.currentTimeMillis()/1000L;
    CexClient client = clientProvider.get();
    String signature = client.getSignature(timestamp);
    String key = client.getKey();
    AuthEvent auth = new AuthEvent();
    auth.setAuth(new AuthEvent.Auth(key, signature, timestamp));
    LOGGER.info("Authentication: {}", auth);
    client.send(gson.toJson(auth));
  }

  @Override
  public void onMessage(AuthEvent event) {
    if (event.isOk()) {
      LOGGER.info("Authentication OK");
      subscribeAll();
    } else {
      LOGGER.error("Authentication failed: '{}'", event.getErrorMessage());
    }
  }

  @Override
  public void onMessage(PingEvent event) {
    LOGGER.info("{}", event);
    clientProvider.get().send(gson.toJson(pong));
  }

  @Override
  public void onMessage(SnapshotEvent event) {
    if (event.isOk()) {
      PriceBasedOrderBook book = bookKeeper.get(event.getPair());
      book.processSnapshot(event);
      producer.publish(book);
    } else {
      LOGGER.error("Subscription failed: {}", event.getErrorMessage());
    }
  }

  @Override
  public void onMessage(MDUpdateEvent event) {
    PriceBasedOrderBook book = bookKeeper.get(event.getPair());
    if (book.isInitialized()) {
      book.processIncrementalUpdate(event.getDeletion());
      book.processIncrementalUpdate(event.getUpdate());
      producer.publish(book);
    }
  }

  private void subscribeAll() {
    CexClient sender = clientProvider.get();
    if (sender == null) {
      LOGGER.error("CexClient unavailable, skip subscription");
      return;
    }
    bookKeeper.getAllPairs().forEach(pair -> {
      SubEvent sub = new SubEvent(pair.getCode());
      sub.sub(pair);
      String subscription = gson.toJson(sub);
      LOGGER.info("Subscribe: {}", subscription);
      sender.send(subscription);
    });
  }
}
