package com.blokaly.ceres.bitfinex;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Singleton
public class BitfinexClientProvider implements Provider<BitfinexClient>, BitfinexClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(BitfinexClientProvider.class);
  private final URI serverURI;
  private final JsonCracker cracker;
  private BitfinexClient client;

  @Inject
  public BitfinexClientProvider(URI serverURI, JsonCracker cracker) {
    this.serverURI = serverURI;
    this.cracker = cracker;
  }

  @Override
  public synchronized BitfinexClient get() {
    if (client == null) {
      client = new BitfinexClient(serverURI, cracker, this);
    }
    return client;
  }

  @Override
  public void onConnected() {

  }

  @Override
  public void onDisconnected() {
    LOGGER.info("Bitfinex client disconnected, making a new one");
    synchronized (this) {
      client = new BitfinexClient(serverURI, cracker, this);
      client.connect();
    }
  }
}
