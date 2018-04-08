package com.blokaly.ceres.web;

import com.blokaly.ceres.binding.CeresService;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.web.handlers.QuoteQueryHandler;
import com.blokaly.ceres.web.handlers.ServerInfoHandler;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.*;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.xnio.Options;

@CeresService
public class UndertowService extends AbstractIdleService {

  private final Undertow server;

  @Inject
  public UndertowService(Undertow server) {
    this.server = server;
  }

  @Override
  protected void startUp() throws Exception {
    server.start();
  }

  @Override
  protected void shutDown() throws Exception {
    server.stop();
  }

  private static class UndertowModule extends PrivateModule {

    @Override
    protected void configure() {
      install(new HandlerModule() {
        @Override
        protected void configureHandlers() {
          bindHandler().to(ServerInfoHandler.class);
          bindHandler().to(QuoteQueryHandler.class);
        }
      });
    }

    @Exposed @Provides @Singleton
    public Undertow provideUndertow(HttpHandler handler) {

      return Undertow.builder()
          .addHttpListener(8080, "localhost")
          .setBufferSize(1024 * 16)
          .setSocketOption(Options.BACKLOG, 10000)
          .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
          .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
          .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
          .setHandler(Handlers.header(handler, Headers.SERVER_STRING, "Ceres"))
          .build();
    }
  }

  public static void main(String[] args) {
    Services.start(new UndertowModule());
  }
}
