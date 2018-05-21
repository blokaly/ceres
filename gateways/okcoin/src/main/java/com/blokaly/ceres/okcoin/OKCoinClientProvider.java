package com.blokaly.ceres.okcoin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class OKCoinClientProvider  implements Provider<OKCoinClient>, OKCoinClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(OKCoinClientProvider.class);
  private final OKCoinClient client;

  @Inject
  public OKCoinClientProvider(URI serverURI, JsonCracker cracker) {
    client = new OKCoinClient(serverURI, cracker, this);
  }

  @Override
  public synchronized OKCoinClient get() {
    return client;
  }

  @Override
  public void onConnected() {
    LOGGER.info("OKCoin client connected");
  }

  @Override
  public void onDisconnected() {
    LOGGER.info("OKCoin client disconnected, reconnecting...");
    client.reconnect();
  }
}
