package com.blokaly.ceres.gdax;

import com.blokaly.ceres.binding.CeresService;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Exchange;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.gdax.callback.*;
import com.blokaly.ceres.gdax.event.AbstractEvent;
import com.blokaly.ceres.gdax.event.EventType;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import com.typesafe.config.Config;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blokaly.ceres.gdax.event.EventType.*;

@CeresService
public class GdaxService extends AbstractIdleService {

  private final Provider<GdaxClient> provider;

  @Inject
  public GdaxService(Provider<GdaxClient> provider) {
    this.provider = provider;
  }

  @Override
  protected void startUp() throws Exception {
    provider.get().connect();
  }

  @Override
  protected void shutDown() throws Exception {
    provider.get().close();
  }

  public static class GdaxModule extends PrivateModule {

    @Override
    protected void configure() {
      install(new CommonModule());
      install(new KafkaCommonModule());
      MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
      binder.addBinding(HB).to(HeartbeatCallbackHandler.class);
      binder.addBinding(SUBS).to(SubscribedCallbackHandler.class);
      binder.addBinding(SNAPSHOT).to(SnapshotCallbackHandler.class);
      binder.addBinding(L2U).to(RefreshCallbackHandler.class);
      bind(MessageHandler.class).to(MessageHandlerImpl.class).in(Singleton.class);
      bind(GdaxClient.class).toProvider(GdaxClientProvider.class).in(Singleton.class);
      expose(GdaxClient.class);
    }

    @Provides
    public URI provideUri(Config config) throws Exception {
      return new URI(config.getString("app.ws.url"));
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
      Exchange exchange = Exchange.valueOf(config.getString("app.exchange").toUpperCase());
      return symbols.stream().collect(Collectors.toMap(sym->sym, sym -> {
        String symbol = SymbolFormatter.normalise(sym);
        return new PriceBasedOrderBook(symbol, symbol + "." + exchange.getCode());
      }));
    }
  }

  public static void main(String[] args) {
    Services.start(new GdaxModule());
  }
}