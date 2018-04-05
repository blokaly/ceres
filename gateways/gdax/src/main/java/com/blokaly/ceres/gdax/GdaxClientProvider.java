package com.blokaly.ceres.gdax;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Singleton
public class GdaxClientProvider implements Provider<GdaxClient>, GdaxClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(GdaxClientProvider.class);
  private final GdaxClient client;

  @Inject
  public GdaxClientProvider(URI serverURI, JsonCracker cracker) {
    client = new GdaxClient(serverURI, cracker, this);
  }

  @Override
  public synchronized GdaxClient get() {
    return client;
  }

  @Override
  public void onConnected() {
    LOGGER.info("Gdax client connected");
  }

  @Override
  public void onDisconnected() {
    LOGGER.info("Gdax client disconnected, reconnecting...");
    client.reconnect();
  }
}
