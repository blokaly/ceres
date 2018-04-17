package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.pusher.client.Client;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class PusherClient implements ConnectionEventListener, ChannelEventListener {

    private static final String BTCUSD = "btcusd";
    private final Logger logger;
    private final String symbol;
    private final Client pusher;
    private final Gson gson;

    private final OrderBookHandler handler;

    public PusherClient(Client pusher, OrderBookHandler handler, Gson gson) {
        this.pusher = pusher;
        this.handler = handler;
        this.gson = gson;
        symbol = handler.getSymbol();
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
        String channel = "diff_order_book" + (BTCUSD.equals(symbol) ? "" : "_" + symbol);
        pusher.subscribe(channel, this, "data");
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
        logger.info("{} subscription succeeded", channelName);
        handler.start();
    }

    @Override
    public void onEvent(String channelName, String eventName, String data) {
        logger.debug("{}:{} - {}", channelName, eventName, data);
        DiffBookEvent diffBookEvent = gson.fromJson(data, DiffBookEvent.class);
        handler.handle(diffBookEvent);
    }

    public void start() {
        pusher.connect(this, ConnectionState.ALL);
    }

    protected void stop() {
        pusher.disconnect();
    }
}
