package com.blokaly.ceres.huobi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Singleton
public class HuobiClientProvider implements Provider<HuobiClient>, HuobiClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(HuobiClientProvider.class);
  private final HuobiClient client;

  @Inject
  public HuobiClientProvider(URI serverURI, JsonCracker cracker) {
    client = new HuobiClient(serverURI, cracker, this);
  }

  @Override
  public synchronized HuobiClient get() {
    return client;
  }

  @Override
  public void onConnected() {
    LOGGER.info("Huobi client connected");
  }

  @Override
  public void onDisconnected() {
    LOGGER.info("Huobi client disconnected, reconnecting...");
    client.reconnect();
  }
}
