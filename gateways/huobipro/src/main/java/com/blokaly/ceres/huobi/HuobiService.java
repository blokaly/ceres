package com.blokaly.ceres.huobi;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.common.Source;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.huobi.callback.*;
import com.blokaly.ceres.huobi.event.EventType;
import com.blokaly.ceres.huobi.event.WSEvent;
import com.blokaly.ceres.kafka.HBProducer;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HuobiService extends BootstrapService {
  private final Provider<HuobiClient> provider;
  private final KafkaStreams streams;

  @Inject
  public HuobiService(Provider<HuobiClient> provider, @Named("Throttled") KafkaStreams streams) {
    this.provider = provider;
    this.streams = streams;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting huobi client...");
    provider.get().connect();

    waitFor(3);
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping huobi client...");
    provider.get().close();
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class HuobiModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      install(new KafkaStreamModule());
      bindExpose(ToBProducer.class);
      bind(HBProducer.class).asEagerSingleton();
      expose(StreamsBuilder.class).annotatedWith(Names.named("Throttled"));
      expose(KafkaStreams.class).annotatedWith(Names.named("Throttled"));

      bindAllCallbacks();
      bind(MessageHandler.class).to(MessageHandlerImpl.class).in(Singleton.class);
      bindExpose(HuobiClient.class).toProvider(HuobiClientProvider.class).in(Singleton.class);
    }

    @Provides
    @Exposed
    public URI provideUri(Config config) throws Exception {
      return new URI(config.getString("app.ws.url"));
    }

    @Provides
    @Singleton
    @Exposed
    public Gson provideGson(Map<EventType, CommandCallbackHandler> handlers) {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(WSEvent.class, new EventAdapter(handlers));
      return builder.create();
    }

    @Provides
    @Singleton
    @Exposed
    public Map<String, PriceBasedOrderBook> provideOrderBooks(Config config) {
      List<String> symbols = config.getStringList("symbols");
      String source = Source.valueOf(config.getString(CommonConfigs.APP_SOURCE).toUpperCase()).getCode();
      return symbols.stream().collect(Collectors.toMap(sym->sym, sym -> {
        String symbol = SymbolFormatter.normalise(sym);
        String pair = symbol.endsWith("usdt") ? symbol.replace("usdt", "usd") : symbol;
        return new PriceBasedOrderBook(symbol, pair + "." + source);
      }));
    }

    private void bindAllCallbacks() {
      MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
      binder.addBinding(EventType.PING).to(PingCallbackHandler.class);
      binder.addBinding(EventType.PONG).to(PongCallbackHandler.class);
      binder.addBinding(EventType.SUBSCRIBED).to(SubscribedCallbackHandler.class);
      binder.addBinding(EventType.TICK).to(SnapshotCallbackHandler.class);
    }
  }

  public static void main(String[] args) {
    Services.start(new HuobiModule());
  }
}