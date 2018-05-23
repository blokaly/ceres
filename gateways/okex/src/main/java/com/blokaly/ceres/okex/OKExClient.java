package com.blokaly.ceres.okex;

import com.blokaly.ceres.common.GzipUtils;
import com.google.inject.Inject;
import org.apache.kafka.common.utils.ByteBufferInputStream;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class OKExClient extends WebSocketClient {

  private static Logger LOGGER = LoggerFactory.getLogger(OKExClient.class);
  private volatile boolean stop = false;
  private final JsonCracker cracker;
  private final ConnectionListener listener;

  public interface ConnectionListener {
    void onConnected();
    void onDisconnected();
  }

  @Inject
  public OKExClient(URI serverURI, JsonCracker cracker, ConnectionListener listener) {
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

  public void onMessage( ByteBuffer bytes ) {
    LOGGER.debug("ws blob message");
    ByteBufferInputStream stream = new ByteBufferInputStream(bytes);
    try {
      String jsonString = GzipUtils.inflate(stream);
      onMessage(jsonString);
    } catch (IOException e) {
      LOGGER.error("Error decompress message", e);
    }
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
