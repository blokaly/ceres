package com.blokaly.ceres.web.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;

@Singleton
public class WebRootHandler extends UndertowGetHandler {

  private final ResourceHandler handler;

  @Inject
  public WebRootHandler(ResourceManager resourceManager, String... files) {
    handler = Handlers.resource(resourceManager);
    if (files.length > 0) {
      handler.addWelcomeFiles(files);
    }
    handler.setDirectoryListingEnabled(true);
  }

  @Override
  public String handlerPath() {
    return "*";
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    handler.handleRequest(exchange);
  }
}
