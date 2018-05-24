package com.blokaly.ceres.quoinex;

import com.google.gson.Gson;
import com.pusher.client.Client;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PusherClient implements ConnectionEventListener, ChannelEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(PusherClient.class);
  private static final String PRICE_SELL_CHANNEL = "price_ladders_cash_%s_sell";
  private static final String PRICE_BUY_CHANNEL = "price_ladders_cash_%s_buy";
  private final Client pusher;
  private final OrderBookHandler handler;
  private final Gson gson;

  public PusherClient(Client pusher, OrderBookHandler handler, Gson gson) {
    this.pusher = pusher;
    this.handler = handler;
    this.gson = gson;
  }

  @Override
  public void onConnectionStateChange(ConnectionStateChange change) {
    LOGGER.info("State changed from {} to {}", change.getPreviousState(), change.getCurrentState());
    if (change.getCurrentState() == ConnectionState.CONNECTED) {
      subscribe();
    }
  }

  @Override
  public void onError(String message, String code, Exception e) {
    LOGGER.error("Pusher connection error: " + message, e);
  }

  private void subscribe() {
    handler.init();
    String sellChannel = String.format(PRICE_SELL_CHANNEL, handler.getSymbol());
    pusher.subscribe(sellChannel, this, "updated");
    String buyChannel = String.format(PRICE_BUY_CHANNEL, handler.getSymbol());
    pusher.subscribe(buyChannel, this, "updated");

  }

  @Override
  public void onSubscriptionSucceeded(String channelName) {
    LOGGER.info("{} subscription succeeded", channelName);
  }

  @Override
  public void onEvent(String channelName, String eventName, String data) {
    LOGGER.debug("{}:{} - {}", channelName, eventName, data);
    OneSidedOrderBookEvent orderBookEvent = null;
    if (channelName.endsWith("buy")) {
      orderBookEvent = gson.fromJson(data, OneSidedOrderBookEvent.BuyOrderBookEvent.class);
    } else if (channelName.endsWith("sell")) {
      orderBookEvent = gson.fromJson(data, OneSidedOrderBookEvent.SellOrderBookEvent.class);
    }

    if (orderBookEvent != null) {
      handler.handle(orderBookEvent);
    }
  }

  public void start() {
    pusher.connect(this, ConnectionState.ALL);
  }

  protected void stop() {
    pusher.disconnect();
  }
}