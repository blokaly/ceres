package com.blokaly.ceres.web;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.xnio.Options;

@Singleton
public class UndertowProvider implements Provider<Undertow> {
  private final Undertow server;

  @Inject
  public UndertowProvider(HttpHandler handler) {
    server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setBufferSize(1024 * 16)
        .setSocketOption(Options.BACKLOG, 10000)
        .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
        .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
        .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
        .setHandler(Handlers.header(handler, Headers.SERVER_STRING, "Ceres"))
        .build();
  }

  @Override
  public Undertow get() {
    return server;
  }
}
