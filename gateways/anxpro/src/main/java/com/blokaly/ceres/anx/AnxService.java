package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.callback.CommandCallbackHandler;
import com.blokaly.ceres.anx.callback.SnapshotCallbackHandler;
import com.blokaly.ceres.anx.event.AbstractEvent;
import com.blokaly.ceres.anx.event.EventType;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Exchange;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.KafkaCommonModule;
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
import com.typesafe.config.Config;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blokaly.ceres.anx.event.EventType.SNAPSHOT;

public class AnxService extends AbstractService {

  private final AnxSocketIOClient client;

  @Inject
  public AnxService(AnxSocketIOClient client) {
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
      install(new CommonModule());
      install(new KafkaCommonModule());
      MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
      binder.addBinding(SNAPSHOT).to(SnapshotCallbackHandler.class);
      bind(MessageHandler.class).to(MessageHandlerImpl.class);
      bind(Service.class).to(AnxService.class);
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
      String exchange = Exchange.valueOf(config.getString("app.exchange").toUpperCase()).getCode();
      return symbols.stream().collect(Collectors.toMap(sym -> sym, sym -> {
        String symbol = SymbolFormatter.normalise(sym);
        return new PriceBasedOrderBook(symbol, symbol + "." + exchange);
      }));
    }
  }

  public static void main(String[] args) {
    Services.start(new AnxModule());
  }
}
