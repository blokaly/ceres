package com.blokaly.ceres.simulation;

import com.blokaly.ceres.binding.AwaitExecutionService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.kafka.GsonProducer;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorService extends AwaitExecutionService {
  private static Logger LOGGER = LoggerFactory.getLogger(SimulatorService.class);
  private final RandomQuote randomQuote;

  @Inject
  public SimulatorService(RandomQuote randomQuote) {
    this.randomQuote = randomQuote;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("Simulator startup");
    randomQuote.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("Simulator shutdown");
    randomQuote.stop();
  }

  private static class SimulationModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      bindExpose(GsonProducer.class);
      bind(Gson.class).toInstance(new GsonBuilder().create());
    }
  }

  public static void main(String[] args) {
    Services.start(new SimulationModule());
  }

}

