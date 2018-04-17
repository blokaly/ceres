package com.blokaly.ceres.web;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Singleton
public class UndertowProvider implements Provider<Undertow> {
  private static final Logger LOGGER = LoggerFactory.getLogger(UndertowProvider.class);
  private final Undertow server;

  @Inject
  public UndertowProvider(HttpHandler handler) {
    try {
      String address = InetAddress.getLocalHost().getHostAddress();
      LOGGER.info("Starting Undertow server: {}:8080", address);
      server = Undertow.builder()
          .addHttpListener(8080, address)
          .setBufferSize(1024 * 16)
          .setSocketOption(Options.BACKLOG, 10000)
          .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
          .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
          .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
          .setHandler(Handlers.header(handler, Headers.SERVER_STRING, "Ceres"))
          .build();
    } catch (UnknownHostException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public Undertow get() {
    return server;
  }
}
