package com.blokaly.ceres.web;

import com.blokaly.ceres.binding.CeresModule;
import com.google.inject.Exposed;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;

import java.util.Set;

public abstract class HandlerModule extends CeresModule {
  private Multibinder<UndertowHandler> handlerBinder;

  @Override
  protected void configure() {
    handlerBinder = Multibinder.newSetBinder(binder(), UndertowHandler.class);
    configureHandlers();
  }

  protected abstract void configureHandlers();

  protected final LinkedBindingBuilder<UndertowHandler> bindHandler() {
    return handlerBinder.addBinding();
  }

  @Exposed @Provides @Singleton
  public HttpHandler provideHttpHandler(Set<UndertowHandler> handlers) {
    RoutingHandler routing = Handlers.routing();
    for (UndertowHandler handler : handlers) {
      routing.add(handler.handlerMethod(), handler.handlerPath(), handler);
    }
    return routing;
  }
}
