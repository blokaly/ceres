package com.blokaly.ceres.cex;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class CexClientProvider implements Provider<CexClient>, CexClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(CexClientProvider.class);
  private final CexClient client;

  @Inject
  public CexClientProvider(Config config, URI serverURI, JsonCracker cracker) {
    client = new CexClient(config, serverURI, cracker, this);
  }

  @Override
  public synchronized CexClient get() {
    return client;
  }

  @Override
  public void onConnected() {
    LOGGER.info("CexClient connected");
  }

  @Override
  public void onDisconnected() {
    LOGGER.info("CexClient disconnected, reconnecting...");
    client.reconnect();
  }
}
