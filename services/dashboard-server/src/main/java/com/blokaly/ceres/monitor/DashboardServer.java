package com.blokaly.ceres.monitor;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.web.HandlerModule;
import com.blokaly.ceres.web.UndertowModule;
import com.google.inject.Inject;
import io.undertow.Undertow;

public class DashboardServer extends BootstrapService {
  private final Undertow server;

  @Inject
  public DashboardServer(Undertow server) {
    this.server = server;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("Web server starting...");
    server.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("Web server stopping...");
    server.stop();
  }

  private static class DashboardServerModule extends CeresModule {

    @Override
    protected void configure() {
      install(new UndertowModule(new HandlerModule() {
        @Override
        protected void configureHandlers() {
          bindHandler().toProvider(DashboardWebSocketProvider.class);
        }
      }));
      expose(Undertow.class);
    }
  }

  public static void main(String[] args) {
    Services.start(new DashboardServerModule());
  }
}


