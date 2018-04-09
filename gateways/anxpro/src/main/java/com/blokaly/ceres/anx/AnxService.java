package com.blokaly.ceres.anx;

import com.blokaly.ceres.anx.callback.CommandCallbackHandler;
import com.blokaly.ceres.anx.callback.SnapshotCallbackHandler;
import com.blokaly.ceres.anx.event.AbstractEvent;
import com.blokaly.ceres.anx.event.EventType;
import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Exchange;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.typesafe.config.Config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blokaly.ceres.anx.event.EventType.SNAPSHOT;

public class AnxService extends BootstrapService {

  private final AnxSocketIOClient client;

  @Inject
  public AnxService(AnxSocketIOClient client) {
    this.client = client;
  }

  @Override
  protected void startUp() throws Exception {
    client.connect();
  }

  @Override
  protected void shutDown() throws Exception {
    client.close();
  }

  public static class AnxModule extends CeresModule {

    @Override
    protected void configure() {
      install(new CommonModule());
      install(new KafkaCommonModule());
      bindAllCallbacks();
      expose(Config.class);
      bindExpose(MessageHandler.class).to(MessageHandlerImpl.class);
    }

    @Provides
    @Singleton
    @Exposed
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

    private void bindAllCallbacks() {
      MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
      binder.addBinding(SNAPSHOT).to(SnapshotCallbackHandler.class);
    }
  }

  public static void main(String[] args) {
    Services.start(new AnxModule());
  }
}
