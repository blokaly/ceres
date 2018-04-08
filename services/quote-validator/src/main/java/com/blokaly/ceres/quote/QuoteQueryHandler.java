package com.blokaly.ceres.quote;

import com.blokaly.ceres.web.handlers.UndertowGetHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@Singleton
public class QuoteQueryHandler extends UndertowGetHandler {
  private final Gson gson;

  @Inject
  public QuoteQueryHandler(Gson gson) {
    this.gson = gson;
  }

  @Override
  public String handlerPath() {
    return "/v1/quote/{quoteId}";
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    String quoteId = exchange.getQueryParameters().get("quoteId").getFirst();
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    JsonObject object = new JsonObject();
    object.addProperty("id", quoteId);
    object.addProperty("status", "OK");
    exchange.getResponseSender().send(gson.toJson(object));
  }
}
