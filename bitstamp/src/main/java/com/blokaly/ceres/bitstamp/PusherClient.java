package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.common.util.concurrent.AbstractService;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pusher.client.Client;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

@Singleton
public class PusherClient extends AbstractService implements ConnectionEventListener, ChannelEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PusherClient.class);

    private final Client pusher;
    private final Gson gson;

    private final OrderBookHandler handler;

    @Inject
    public PusherClient(Client pusher, Gson gson, ExecutorService ses) {
        this.pusher = pusher;
        this.gson = gson;
        handler = new OrderBookHandler(new PriceBasedOrderBook("btcusd"), gson, ses);
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
        pusher.subscribe("diff_order_book", this, "data");
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
        LOGGER.info("{} subscription succeeded", channelName);
        handler.start();
    }

    @Override
    public void onEvent(String channelName, String eventName, String data) {
        LOGGER.info("{}:{} - {}", channelName, eventName, data);
        DiffBookEvent diffBookEvent = gson.fromJson(data, DiffBookEvent.class);
        handler.handle(diffBookEvent);
    }

    @Override
    protected void doStart() {
        pusher.connect(this, ConnectionState.ALL);
    }

    @Override
    @PreDestroy
    protected void doStop() {
        pusher.disconnect();
    }
}
