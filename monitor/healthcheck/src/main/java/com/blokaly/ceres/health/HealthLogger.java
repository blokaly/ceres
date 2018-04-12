package com.blokaly.ceres.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthLogger {
  public static Logger getLogger(Class classz) {
    return getLogger(classz.getSimpleName());
  }

  public static Logger getLogger(String name) {
    return LoggerFactory.getLogger("com.blokaly.monitor.#" + name);
  }
}
