package com.blokaly.ceres.bittrex;

import microsoft.aspnet.signalr.client.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalRLogger implements microsoft.aspnet.signalr.client.Logger {

  private static final Logger LOGGER = LoggerFactory.getLogger(SignalRLogger.class);
  @Override
  public void log(String message, LogLevel logLevel) {
    if (logLevel == null) {
      LOGGER.info(message);
    } else {
      switch (logLevel) {
        case Verbose:
          LOGGER.debug(message);
          break;
        case Information:
          LOGGER.info(message);
          break;
        case Critical:
          LOGGER.error(message);
          break;
      }
    }
  }

  @Override
  public boolean isVerbose() {
    return LOGGER.isDebugEnabled();
  }
}
