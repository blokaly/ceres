package com.blokaly.ceres.web;

import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;

public interface UndertowHandler extends HttpHandler {
  HttpString handlerMethod();
  String handlerPath();
}
