package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.callback.CommandCallbackHandler;
import com.blokaly.ceres.anx.callback.SnapshotCallbackHandler;
import com.blokaly.ceres.anx.event.AbstractEvent;
import com.blokaly.ceres.anx.event.EventType;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.DumpAndShutdownModule;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.KafkaModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.netflix.governator.InjectorBuilder;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.blokaly.ceres.anx.event.EventType.SNAPSHOT;

public class AnxApp extends AbstractService {

  private final AnxSocketIOClient client;

  @Inject
  public AnxApp(AnxSocketIOClient client) {
    this.client = client;
  }

  @Override
  protected void doStart() {
    client.connect();
  }

  @Override
  @PreDestroy
  protected void doStop() {
    client.close();
  }

  public static class AnxModule extends AbstractModule {

    @Override
    protected void configure() {
      MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
      binder.addBinding(SNAPSHOT).to(SnapshotCallbackHandler.class);
      bind(MessageHandler.class).to(MessageHandlerImpl.class);
      bind(Service.class).to(AnxApp.class);
    }

    @Provides
    @Singleton
    public Gson provideGson(Map<EventType, CommandCallbackHandler> handlers) {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(AbstractEvent.class, new EventAdapter(handlers));
      return builder.create();
    }

    @Provides
    @Singleton
    public Map<String, PriceBasedOrderBook> provideOrderBooks(Config config) {
      List<String> symbols = config.getStringList("symbols");
      String appName = config.getString("app.name");
      return symbols.stream().collect(Collectors.toMap(sym -> sym, sym -> {
        String symbol = SymbolFormatter.normalise(sym);
        return new PriceBasedOrderBook(symbol, symbol + "." + appName);
      }));
    }
  }

  public static void main(String[] args) throws Exception {
    InjectorBuilder.fromModules(new DumpAndShutdownModule(), new CommonModule(), new KafkaModule(), new AnxModule())
        .createInjector()
        .getInstance(Service.class)
        .startAsync().awaitTerminated();
  }
}
