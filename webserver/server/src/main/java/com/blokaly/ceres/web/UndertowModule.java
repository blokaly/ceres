package com.blokaly.ceres.web;

import com.blokaly.ceres.binding.CeresModule;
import com.google.inject.Singleton;
import io.undertow.Undertow;

public class UndertowModule extends CeresModule {
  private final HandlerModule handlerModule;

  public UndertowModule(HandlerModule handlerModule) {
    this.handlerModule = handlerModule;
  }

  @Override
  protected void configure() {
    install(handlerModule);
    bindExpose(Undertow.class).toProvider(UndertowProvider.class).in(Singleton.class);
  }

}
