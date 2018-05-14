package com.blokaly.ceres.quoinex;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.common.Source;
import com.blokaly.ceres.data.SymbolFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.typesafe.config.Config;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class QuoinexService extends BootstrapService {
  private final List<PusherClient> clients;
  @Inject
  public QuoinexService(List<PusherClient> clients) {
    this.clients = clients;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting pusher client...");
    clients.forEach(PusherClient::start);
    awaitTerminated();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping pusher client...");
    clients.forEach(PusherClient::stop);
  }



  public static class QuoinexModule extends CeresModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Exposed
    public List<PusherClient> providePusherClients(Config config, Gson gson, @SingleThread Provider<ExecutorService> provider) {
      String subId = config.getString("app.pusher.key");
      String source = Source.valueOf(config.getString(CommonConfigs.APP_SOURCE).toUpperCase()).getCode();
      PusherOptions options = new PusherOptions();
      return config.getStringList("symbols").stream()
          .map(sym -> {
            String symbol = SymbolFormatter.normalise(sym);
            return new PusherClient(new Pusher(subId, options), symbol, gson);
          })
          .collect(Collectors.toList());
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
