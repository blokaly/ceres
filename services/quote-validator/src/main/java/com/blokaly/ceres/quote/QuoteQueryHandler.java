package com.blokaly.ceres.quote;

import com.blokaly.ceres.redis.RedisClient;
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
  private final RedisClient redis;

  @Inject
  public QuoteQueryHandler(Gson gson, RedisClient redis) {
    this.gson = gson;
    this.redis = redis;
  }

  @Override
  public String handlerPath() {
    return "/v1/quote/{quoteId}";
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    String quoteId = exchange.getQueryParameters().get("quoteId").getFirst();
    JsonObject object = new JsonObject();
    object.addProperty("id", quoteId);
    String quote = redis.get(quoteId);
    if (quote == null) {
      object.addProperty("status", "UNKNOWN");
    } else {
      object.addProperty("status", "OK");
    }
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    exchange.getResponseSender().send(gson.toJson(object));
  }
}
