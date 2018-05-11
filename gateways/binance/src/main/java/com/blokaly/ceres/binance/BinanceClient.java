package com.blokaly.ceres.binance;

import com.blokaly.ceres.binance.event.DiffBookEvent;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URI;

public class BinanceClient extends WebSocketClient {

  private static Logger LOGGER = LoggerFactory.getLogger(BinanceClient.class);
  private volatile boolean stop = false;
  private final OrderBookHandler handler;
  private final Gson gson;
  private final ConnectionListener listener;

  public interface ConnectionListener {
    void onConnected(String symbol);
    void onDisconnected(String symbol);
  }

  public BinanceClient(URI serverURI, OrderBookHandler handler, Gson gson, ConnectionListener listener) {
    super(serverURI);
    this.handler = handler;
    this.gson = gson;
    this.listener = listener;
    LOGGER.info("client initiated");
  }



  @Override
  public void onOpen(ServerHandshake handshake) {
    LOGGER.info("ws open, status - {}:{}", handshake.getHttpStatus(), handshake.getHttpStatusMessage());
    if (listener != null) {
      listener.onConnected(handler.getSymbol());
      handler.start();
    }
  }

  @Override
  public void onMessage(String message) {
    LOGGER.debug("ws message: {}", message);
    if (!stop) {
      DiffBookEvent diffBookEvent = gson.fromJson(message, DiffBookEvent.class);
      handler.handle(diffBookEvent);
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    LOGGER.info("ws close: {}", reason);
    if (listener != null) {
      listener.onDisconnected(handler.getSymbol());
    }
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
