package com.blokaly.ceres.common;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.netflix.governator.InjectorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Services {
  private static Logger LOGGER = LoggerFactory.getLogger(Services.class);

  public static void start(AbstractModule module) {
    Service service = InjectorBuilder.fromModules(module)
        .createInjector()
        .getInstance(Service.class)
        .startAsync();
    try {
      service.awaitTerminated();
    } catch (IllegalStateException ise) {
      LOGGER.error("Service failed to start", service.failureCause());
      System.exit(1);
    }
  }
}
