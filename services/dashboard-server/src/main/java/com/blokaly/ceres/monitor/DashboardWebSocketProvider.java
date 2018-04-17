package com.blokaly.ceres.monitor;

import com.blokaly.ceres.web.handlers.UndertowWebSocketHandler;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DashboardWebSocketProvider implements Provider<UndertowWebSocketHandler> {

  private final UndertowWebSocketHandler handler;

  public DashboardWebSocketProvider() {
    handler = new UndertowWebSocketHandler("/api/ws", new DashboardWebSocketCallback());
  }

  @Override
  public UndertowWebSocketHandler get() {
    return handler;
  }


  private static class DashboardWebSocketCallback implements WebSocketConnectionCallback {
    private final Logger LOGGER = LoggerFactory.getLogger(DashboardWebSocketCallback.class);
    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
      LOGGER.info("WebSocket channel {} connected", channel.getSourceAddress());
      channel.getReceiveSetter().set(new AbstractReceiveListener() {
        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
          final String messageData = message.getData();
          for (WebSocketChannel session : channel.getPeerConnections()) {
            WebSockets.sendText("dashboard say: " + messageData, session, null);
          }
        }
      });
      channel.resumeReceives();
    }
  }
}
