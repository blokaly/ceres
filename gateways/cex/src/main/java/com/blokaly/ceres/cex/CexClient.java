package com.blokaly.ceres.cex;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.apache.commons.codec.binary.Hex;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;

public class CexClient extends WebSocketClient {

  private static Logger LOGGER = LoggerFactory.getLogger(CexClient.class);
  private final String key;
  private final String secret;
  private volatile boolean stop = false;
  private final JsonCracker cracker;
  private final ConnectionListener listener;

  public interface ConnectionListener {
    void onConnected();
    void onDisconnected();
  }

  @Inject
  public CexClient(Config config, URI serverURI, JsonCracker cracker, ConnectionListener listener) {
    super(serverURI);
    this.cracker = cracker;
    this.listener = listener;
    Config apiConfig = config.getConfig("api");
    key = apiConfig.getString("key");
    secret = apiConfig.getString("secret");
  }

  public String getKey() {
    return key;
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

  public String getSignature(long timestamp) {
    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
      sha256_HMAC.init(secret_key);

      String what = Long.toString(timestamp) + key;
      return Hex.encodeHexString(sha256_HMAC.doFinal(what.getBytes()));
    } catch (Exception e) {
      LOGGER.error("Failed to generate signature for {}, ts:{}", key, timestamp);
      return null;
    }
  }
}
