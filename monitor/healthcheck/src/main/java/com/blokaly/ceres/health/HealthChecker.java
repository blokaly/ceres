package com.blokaly.ceres.health;

import com.codahale.metrics.health.HealthCheck;

public interface HealthChecker {
  HealthCheck getChecker();
  HealthCheck.Result diagnosis();
}
