package com.blokaly.ceres.common;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.netflix.governator.InjectorBuilder;

public class Services {

  public static void start(AbstractModule module) {
    InjectorBuilder.fromModules(module)
        .createInjector()
        .getInstance(Service.class)
        .startAsync().awaitTerminated();
  }
}
