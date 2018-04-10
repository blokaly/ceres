package com.blokaly.ceres.binding;

import com.blokaly.ceres.health.HealthChecker;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.AbstractIdleService;

@CeresService
public abstract class BootstrapService extends AbstractIdleService implements HealthChecker{

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
}
