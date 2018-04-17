package com.blokaly.ceres.binding;

import com.blokaly.ceres.health.HealthChecker;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@CeresService
public abstract class BootstrapService extends AbstractIdleService implements HealthChecker{
  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

  private final HealthCheck checker = new HealthCheck() {
    @Override
    protected Result check() throws Exception {
      return BootstrapService.this.diagnosis();
    }
  };

  @Override
  public HealthCheck getChecker() {
    return checker;
  }

  @Override
  public HealthCheck.Result diagnosis() {
    return HealthCheck.Result.healthy();
  }

  protected void waitFor(int seconds) {
    LOGGER.info("Waiting for {} seconds", seconds);
    try {
      Thread.sleep(TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      LOGGER.warn("Waiting interrupted");
    }
  }

  @Override
  protected void shutDown() throws Exception { }
}
