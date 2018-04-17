package com.blokaly.ceres.monitor;

import com.blokaly.ceres.web.UndertowHandler;
import com.blokaly.ceres.web.handlers.WebRootHandler;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.undertow.server.handlers.resource.ClassPathResourceManager;

@Singleton
public class DashboardWebRootProvider implements Provider<UndertowHandler> {

  @Override
  public UndertowHandler get() {
    return new WebRootHandler(new ClassPathResourceManager(DashboardWebSocketProvider.class.getClassLoader(), "www"), "index.html");
  }
}
