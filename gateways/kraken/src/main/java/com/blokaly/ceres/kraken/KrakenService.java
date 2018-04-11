package com.blokaly.ceres.kraken;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.Exchange;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.google.inject.Exposed;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kraken.service.KrakenMarketDataServiceRaw;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KrakenService extends BootstrapService {

  private final MarketDataHandler handler;

  @Inject
  public KrakenService(MarketDataHandler handler) {
    this.handler = handler;
  }

  @Override
  protected void startUp() throws Exception {
    handler.start();
  }

  @Override
  protected void shutDown() throws Exception {
    handler.stop();
  }

  public static class KrakenModule extends CeresModule {

    @Override
    protected void configure() {
      bind(org.knowm.xchange.Exchange.class).toInstance(ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName()));
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
  }

  public static void main(String[] args) {
    Services.start(new KrakenModule());
  }
}
