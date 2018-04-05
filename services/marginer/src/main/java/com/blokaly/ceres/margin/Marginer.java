package com.blokaly.ceres.margin;

import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.DumpAndShutdownModule;
import com.blokaly.ceres.common.Exchange;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.governator.InjectorBuilder;
import com.typesafe.config.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Marginer extends AbstractService {
  private static final Logger LOGGER = LoggerFactory.getLogger(Marginer.class);
  private final KafkaStreams streams;

  @Inject
  public Marginer(KafkaStreams streams) {
    this.streams = streams;
  }

  @Override
  protected void doStart() {
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  @PreDestroy
  protected void doStop() {
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class MarginerModule extends AbstractModule {

    @Override
    protected void configure() {
      install(new CommonModule());
      install(new KafkaStreamModule());
      bind(Service.class).to(Marginer.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      return builder.create();
    }

    @Provides
    @Singleton
    public KeyValueMapper<String, String, KeyValue<String, String>> provideMarginProcessor(IDSequencer sequencer, Gson gson) {
      return new MarginProcessor(sequencer, gson);
    }

    @Provides
    @Singleton
    public StreamsBuilder provideStreamsBuilder(Config config, KeyValueMapper<String, String, KeyValue<String, String>> marginProcessor) {
      StreamsBuilder builder = new StreamsBuilder();
      List<String> symbols = config.getStringList("symbols");
      KStream<String, String> stream = builder.stream(symbols.stream().map(sym -> "md." + sym).collect(Collectors.toList()));
      stream.filter((key, value) -> key.endsWith(Exchange.BEST.getCode())).map(marginProcessor).to("md." + Exchange.FINFABRIK.name().toLowerCase());
      return builder;
    }
  }

  public static void main(String[] args) throws Exception {
    InjectorBuilder.fromModules(new MarginerModule())
        .createInjector()
        .getInstance(Service.class)
        .startAsync().awaitTerminated();
  }
}
