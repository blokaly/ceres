package com.blokaly.ceres.web.handlers;

import com.blokaly.ceres.web.UndertowHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;

public class UndertowWebSocketHandler extends WebSocketProtocolHandshakeHandler implements UndertowHandler {

  private final String path;

  public UndertowWebSocketHandler(String path, WebSocketConnectionCallback callback) {
    super(callback);
    this.path = path;
  }

  @Override
  public HttpString handlerMethod() {
    return Methods.GET;
  }

  @Override
  public String handlerPath() {
    return path;
  }

}
