package com.blokaly.ceres.gdax;

import com.blokaly.ceres.gdax.event.OpenEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URI;

@Singleton
public class GdaxClient extends WebSocketClient {

  private static Logger LOGGER = LoggerFactory.getLogger(GdaxClient.class);
  private volatile boolean stop = false;
  private final JsonCracker cracker;
  private final ConnectionListener listener;

  public interface ConnectionListener {
    void onConnected();
    void onDisconnected();
  }

  @Inject
  public GdaxClient(URI serverURI, JsonCracker cracker, ConnectionListener listener) {
    super(serverURI);
    this.cracker = cracker;
    this.listener = listener;
    LOGGER.info("client initiated");
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    LOGGER.info("ws open, status: {}:{}", handshakedata.getHttpStatus(), handshakedata.getHttpStatusMessage());
    if (listener != null) {
      listener.onConnected();
    }
    cracker.onOpen();
  }

  @Override
  public void onMessage(String message) {
    LOGGER.debug("ws message: {}", message);
    if (!stop) {
      cracker.crack(message);
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOGGER.info("ws close: {}", reason);
    if (listener != null) {
      listener.onDisconnected();
    }
    cracker.onClose();
  }

  @Override
  public void onError(Exception ex) {
    LOGGER.error("ws error", ex);
  }

  @PreDestroy
  public void stop() {
    stop = true;
    super.close();
  }
}
