package com.blokaly.ceres.quoinex;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.common.Source;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.HBProducer;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class QuoinexService extends BootstrapService {
  private final List<PusherClient> clients;
  private final KafkaStreams streams;

  @Inject
  public QuoinexService(List<PusherClient> clients, @Named("Throttled") KafkaStreams streams) {
    this.clients = clients;
    this.streams = streams;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting pusher client...");
    clients.forEach(PusherClient::start);

    waitFor(3);
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping pusher client...");
    clients.forEach(PusherClient::stop);

    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class QuoinexModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      install(new KafkaStreamModule());
      bindExpose(ToBProducer.class);
      bind(HBProducer.class).asEagerSingleton();

      expose(StreamsBuilder.class).annotatedWith(Names.named("Throttled"));
      expose(KafkaStreams.class).annotatedWith(Names.named("Throttled"));
    }

    @Provides
    @Singleton
    @Exposed
    public List<PusherClient> providePusherClients(Config config, Gson gson, ToBProducer producer) {
      String subId = config.getString("app.pusher.key");
      String source = Source.valueOf(config.getString(CommonConfigs.APP_SOURCE).toUpperCase()).getCode();
      PusherOptions options = new PusherOptions();
      return config.getStringList("symbols").stream()
          .map(sym -> {
            String symbol = SymbolFormatter.normalise(sym);
            DepthBasedOrderBook orderBook = new DepthBasedOrderBook(symbol, 10, symbol + "." + source);
            return new PusherClient(new Pusher(subId, options), new OrderBookHandler(orderBook, producer), gson);
          }).collect(Collectors.toList());
    }

    @Provides
    @Singleton
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(OneSidedOrderBookEvent.BuyOrderBookEvent.class, new OrderBookEventAdapter.BuySideOrderBookEventAdapter());
      builder.registerTypeAdapter(OneSidedOrderBookEvent.SellOrderBookEvent.class, new OrderBookEventAdapter.SellSideOrderBookEventAdapter());
      return builder.create();
    }
  }

  public static void main(String[] args) {
    Services.start(new QuoinexModule());
  }
}
