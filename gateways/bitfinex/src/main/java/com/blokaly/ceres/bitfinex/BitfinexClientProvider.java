package com.blokaly.ceres.bitfinex;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URI;

@Singleton
public class BitfinexClientProvider implements Provider<BitfinexClient>, BitfinexClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(BitfinexClientProvider.class);
  private final BitfinexClient client;
  private volatile boolean stopping;

  @Inject
  public BitfinexClientProvider(URI serverURI, JsonCracker cracker) {
    client = new BitfinexClient(serverURI, cracker, this);
    stopping = false;
  }

  @Override
  public synchronized BitfinexClient get() {
    return client;
  }

  @Override
  public void onConnected() {
    LOGGER.info("Bitfinex client connected");
  }

  @Override
  public void onDisconnected() {
    if (!stopping) {
      LOGGER.info("Bitfinex client disconnected, reconnecting...");
      client.reconnect();
    }
  }

  @PreDestroy
  private void stop() {
    stopping = true;
  }
}
