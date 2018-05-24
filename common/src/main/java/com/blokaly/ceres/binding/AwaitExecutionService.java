package com.blokaly.ceres.binding;

@CeresService
public abstract class AwaitExecutionService extends ExecutionService {
  @Override
  protected void run() throws Exception {
    awaitTerminated();
  }
}
