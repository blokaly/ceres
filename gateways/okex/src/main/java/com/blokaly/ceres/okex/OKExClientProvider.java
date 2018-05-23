package com.blokaly.ceres.okex;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class OKExClientProvider implements Provider<OKExClient>, OKExClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(OKExClientProvider.class);
  private final OKExClient client;

  @Inject
  public OKExClientProvider(URI serverURI, JsonCracker cracker) {
    client = new OKExClient(serverURI, cracker, this);
  }

  @Override
  public synchronized OKExClient get() {
    return client;
  }

  @Override
  public void onConnected() {
    LOGGER.info("OKEx client connected");
  }

  @Override
  public void onDisconnected() {
    LOGGER.info("OKEx client disconnected, reconnecting...");
    client.reconnect();
  }
}
