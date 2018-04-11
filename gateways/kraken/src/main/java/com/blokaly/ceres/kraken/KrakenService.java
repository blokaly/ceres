package com.blokaly.ceres.kraken;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.Exchange;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.HBProducer;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.service.KrakenMarketDataServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KrakenService extends BootstrapService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KrakenService.class);
  private final MarketDataHandler handler;
  private final KafkaStreams streams;

  @Inject
  public KrakenService(MarketDataHandler handler, KafkaStreams streams) {
    this.handler = handler;
    this.streams = streams;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting kraken market data handler...");
    handler.start();
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping kraken market data handler...");
    handler.stop();
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class KrakenModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      install(new KafkaStreamModule());
      bind(org.knowm.xchange.Exchange.class).toInstance(ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName()));
      bindExpose(ToBProducer.class);
      bind(HBProducer.class).asEagerSingleton();
      expose(KafkaStreams.class);
    }

    @Provides
    @Singleton
    @Exposed
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      return builder.create();
    }

    @Exposed
    @Provides
    @Singleton
    public KrakenMarketDataServiceRaw provideMarketDataService(org.knowm.xchange.Exchange exchange) {
      return (KrakenMarketDataServiceRaw) exchange.getMarketDataService();
    }

    @Exposed
    @Provides
    @Singleton
    public Map<String, DepthBasedOrderBook> provideOrderBooks(Config config) {
      List<String> symbols = config.getStringList("symbols");
      int depth = config.getInt("depth");
      Exchange exchange = Exchange.valueOf(config.getString("app.exchange").toUpperCase());
      return symbols.stream().collect(Collectors.toMap(sym->sym, sym -> {
        String symbol = SymbolFormatter.normalise(sym);
        return new DepthBasedOrderBook(symbol, depth, symbol + "." + exchange.getCode());
      }));
    }

    @Provides
    @Singleton
    public StreamsBuilder provideStreamsBuilder(Config config) {
      StreamsBuilder builder = new StreamsBuilder();
      String topic = config.getString(CommonConfigs.KAFKA_TOPIC);
      int windowSecond = config.getInt(CommonConfigs.KAFKA_THROTTLE_SECOND);
      builder.stream(topic)
          .groupByKey()
          .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(windowSecond)))
          .reduce((agg, v)->v)
          .toStream((k,v)->k.key())
          .to(topic + ".throttled");
      return builder;
    }
  }

  public static void main(String[] args) {
    Services.start(new KrakenModule());
  }
}
