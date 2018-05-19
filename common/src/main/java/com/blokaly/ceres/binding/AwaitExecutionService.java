package com.blokaly.ceres.binding;

public class AwaitExecutionService extends ExecutionService {
  @Override
  protected void run() throws Exception {
    awaitTerminated();
  }
}
