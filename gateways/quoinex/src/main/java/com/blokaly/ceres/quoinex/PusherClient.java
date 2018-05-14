package com.blokaly.ceres.quoinex;

import com.blokaly.ceres.data.DepthBasedOrderInfo;
import com.blokaly.ceres.data.MarketDataSnapshot;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.google.gson.Gson;

import com.pusher.client.Client;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

public class PusherClient implements ConnectionEventListener, ChannelEventListener {

  private final Logger logger;
  private final String symbol;
  private final Client pusher;
  private final Gson gson;
  private final DepthBasedOrderBook orderBook = new DepthBasedOrderBook("btcusd", 10, "btcusd");

  public PusherClient(Client pusher, String symbol, Gson gson) {
    this.pusher = pusher;
    this.symbol = symbol;
    this.gson = gson;
    logger = LoggerFactory.getLogger(getClass().getName() + "[" + symbol + "]");
  }

  @Override
  public void onConnectionStateChange(ConnectionStateChange change) {
    logger.info("State changed from {} to {}", change.getPreviousState(), change.getCurrentState());
    if (change.getCurrentState() == ConnectionState.CONNECTED) {
      subscribe();
    }
  }

  @Override
  public void onError(String message, String code, Exception e) {
    logger.error("Pusher connection error: " + message, e);
  }

  private void subscribe() {
    orderBook.processSnapshot(new MarketDataSnapshot<DepthBasedOrderInfo>() {

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
    });

    String sellChannel = String.format("price_ladders_cash_%s_sell", symbol);
    pusher.subscribe(sellChannel, this, "updated");
    String buyChannel = String.format("price_ladders_cash_%s_buy", symbol);
    pusher.subscribe(buyChannel, this, "updated");

  }

  @Override
  public void onSubscriptionSucceeded(String channelName) {
    logger.info("{} subscription succeeded", channelName);
  }

  @Override
  public void onEvent(String channelName, String eventName, String data) {
    logger.debug("{}:{} - {}", channelName, eventName, data);
    OneSidedOrderBookEvent orderBookEvent = null;
    if (channelName.endsWith("buy")) {
      orderBookEvent = gson.fromJson(data, OneSidedOrderBookEvent.BuyOrderBookEvent.class);
    } else if (channelName.endsWith("sell")) {
      orderBookEvent = gson.fromJson(data, OneSidedOrderBookEvent.SellOrderBookEvent.class);
    }

    if (orderBookEvent != null) {
      orderBook.processIncrementalUpdate(orderBookEvent);
      logger.info("{}", orderBook);
    }
  }

  public void start() {
    pusher.connect(this, ConnectionState.ALL);
  }

  protected void stop() {
    pusher.disconnect();
  }
}