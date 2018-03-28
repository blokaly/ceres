package com.blokaly.ceres.stream;

import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.DumpAndShutdownModule;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SmartQuote extends AbstractService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SmartQuote.class);
  private final KafkaStreams streams;

  @Inject
  public SmartQuote(KafkaStreams streams) {
    this.streams = streams;
  }

  @Override
  protected void doStart() {
    streams.start();
  }

  @Override
  @PreDestroy
  protected void doStop() {
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class SmartQuoteModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Service.class).to(SmartQuote.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      return builder.create();
    }

    @Provides
    @Singleton
    public Map<String, BestTopOfBook> AggregatedOrderBooks(Config config) {
      List<String> symbols = config.getStringList("symbols");
      return symbols.stream().collect(Collectors.toMap(sym -> sym, BestTopOfBook::new));
    }

    @Provides
    @Singleton
    public StreamsBuilder provideStreamsBuilder(Config config, ToBMapper mapper, HBProcessor hbProcessor) {
      Config kafka = config.getConfig("kafka");
      StreamsBuilder builder = new StreamsBuilder();
      KStream<String, String> stream = builder.stream(kafka.getStringList("topics"));
      stream.filter((key, value) -> key.startsWith("hb")).foreach(hbProcessor::process);
      List<String> symbols = config.getStringList("symbols");
      KStream<String, String>[] streams = stream.branch(symbols.stream().map(SymbolFilter::new).toArray(SymbolFilter[]::new));
      for (int i = 0; i < streams.length; i++) {
        String topic = "md." + symbols.get(i);
        KStream<String, String> outStream = streams[i];
        outStream.map(mapper).to(topic);
        outStream.to(topic);
      }
      return builder;
    }

    @Provides
    @Singleton
    public KafkaStreams provideKafkaStreams(StreamsBuilder builder, Config config, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
      Properties props = new Properties();
      Config kafka = config.getConfig("kafka");
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafka.getString(StreamsConfig.APPLICATION_ID_CONFIG));
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
      props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
      props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
      Topology topology = builder.build();
      KafkaStreams streams = new KafkaStreams(topology, props);
      streams.setUncaughtExceptionHandler(uncaughtExceptionHandler);
      return streams;
    }
  }

  public static void main(String[] args) throws Exception {
    InjectorBuilder.fromModules(new DumpAndShutdownModule(), new CommonModule(), new SmartQuoteModule())
        .createInjector()
        .getInstance(Service.class)
        .startAsync().awaitTerminated();
  }

}
