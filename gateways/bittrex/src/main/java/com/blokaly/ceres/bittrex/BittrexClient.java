package com.blokaly.ceres.bittrex;

import com.blokaly.ceres.bittrex.event.ExchangeDeltaEvent;
import com.blokaly.ceres.common.GzipUtils;
import com.blokaly.ceres.common.PairSymbol;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubException;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static microsoft.aspnet.signalr.client.ConnectionState.Connected;
import static microsoft.aspnet.signalr.client.ConnectionState.Connecting;

public class BittrexClient implements StateChangedCallback, SubscriptionHandler1<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BittrexClient.class);
  private final HubConnection connection;
  private final HubProxy proxy;
  private final OrderBookKeeper keeper;
  private final Gson gson;

  @Inject
  public BittrexClient(Config config, OrderBookKeeper keeper, Gson gson) {
    this.keeper = keeper;
    this.gson = gson;
    Config signalr = config.getConfig("signalr");
    connection = new HubConnection(signalr.getString("endpoint"), null, true, new SignalRLogger());
    proxy = connection.createHubProxy(signalr.getString("hub"));
  }

  public String requestSnapshot(String market) throws Exception {
    String encoded = proxy.invoke(String.class, "QueryExchangeState", market).get(10L, TimeUnit.SECONDS);
    if (encoded != null) {
      byte[] decoded = Base64.getDecoder().decode(encoded);
      return GzipUtils.inflateRaw(decoded);
    } else {
      throw new HubException("Failed to retrieve snapshot from QueryExchangeState for " + market, null);
    }
  }

  public void subscribe(String market) {
    proxy.invoke("SubscribeToExchangeDeltas", market);
  }

  public void start() {
    connection.stateChanged(this);
    proxy.on("uE", this, String.class);
    connection.start();
  }

  @Override
  public void run(String encoded) {
    try {
      byte[] decoded = Base64.getDecoder().decode(encoded);
      String message = GzipUtils.inflateRaw(decoded);
      LOGGER.debug("delta: {}", message);
      ExchangeDeltaEvent deltaEvent = gson.fromJson(message, ExchangeDeltaEvent.class);
      PairSymbol pair = PairSymbol.parse(deltaEvent.getMarket(), "-");
      OrderBookHandler handler = keeper.get(pair);
      if (handler == null) {
        LOGGER.error("No order book handler found for {}", pair);
      } else {
        handler.handle(deltaEvent);
      }
    } catch (Exception ex) {
      LOGGER.error("Error processing update", ex);
    }

  }

  public void stop() {
    connection.stop();
  }

  @Override
  public void stateChanged(ConnectionState oldState, ConnectionState newState) {
    LOGGER.info("Connection state changed: {} -> {}", oldState, newState);
    if (oldState == Connecting && newState == Connected) {
      keeper.init();
    }
  }


}
