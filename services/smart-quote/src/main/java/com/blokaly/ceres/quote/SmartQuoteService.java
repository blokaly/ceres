package com.blokaly.ceres.quote;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmartQuoteService extends BootstrapService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SmartQuoteService.class);
  private final KafkaStreams streams;

  @Inject
  public SmartQuoteService(KafkaStreams streams) {
    this.streams = streams;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class SmartQuoteModule extends CeresModule {

    @Override
    protected void configure() {
      install(new CommonModule());
      install(new KafkaStreamModule());
      expose(KafkaStreams.class);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      return builder.create();
    }

    @Provides
    @Singleton
    public Map<String, BestTopOfBook> provideTopOfBooks(Config config) {
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
  }

  public static void main(String[] args)  {
    Services.start(new SmartQuoteModule());
  }

}
