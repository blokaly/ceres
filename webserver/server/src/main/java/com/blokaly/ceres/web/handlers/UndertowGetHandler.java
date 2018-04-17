package com.blokaly.ceres.web.handlers;

import com.blokaly.ceres.web.UndertowHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

public abstract class UndertowGetHandler implements UndertowHandler{
  @Override
  public HttpString handlerMethod() {
    return Methods.GET;
  }
}
