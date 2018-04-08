package com.blokaly.ceres.web.handlers;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@Singleton
public class ServerInfoHandler extends UndertowGetHandler {
  private final Gson gson;
  private final ServerInfo serverInfo;

  @Inject
  public ServerInfoHandler(Gson gson) {
    this.gson = gson;
    serverInfo = new ServerInfo("ceres", 1);
  }

  @Override
  public String handlerPath() {
    return "/v1/server/info";
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    exchange.getResponseSender().send(gson.toJson(serverInfo));
  }

  private class ServerInfo {
    private final String name;
    private final int version;

    private ServerInfo(String name, int version) {
      this.name = name;
      this.version = version;
    }
  }
}
