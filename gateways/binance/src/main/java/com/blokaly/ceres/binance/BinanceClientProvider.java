package com.blokaly.ceres.binance;

import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.common.Source;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BinanceClientProvider implements Provider<Collection<BinanceClient>>, BinanceClient.ConnectionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(BinanceClientProvider.class);
  private final String wsUrl;
  private final Gson gson;
  private final ToBProducer producer;
  private final Provider<ExecutorService> esProvider;
  private final Map<String, BinanceClient> clients;
  private final List<String> symbols;
  private volatile boolean stopping;
  private final String source;

  @Inject
  public BinanceClientProvider(Config config, Gson gson, ToBProducer producer, @SingleThread Provider<ExecutorService> esProvider) {
    wsUrl = config.getString(CommonConfigs.WS_URL);
    source = Source.valueOf(config.getString(CommonConfigs.APP_SOURCE).toUpperCase()).getCode();
    symbols = config.getStringList("symbols");
    this.gson = gson;
    this.producer = producer;
    this.esProvider = esProvider;
    clients = Maps.newHashMap();
    stopping = false;
  }

  @PostConstruct
  private void init() {

    symbols.forEach(sym -> {
      try {
        String symbol = SymbolFormatter.normalise(sym);
        URI uri = new URI(String.format(wsUrl, sym));
        OrderBookHandler handler = new OrderBookHandler(new PriceBasedOrderBook(symbol, symbol + "." + source), producer, gson, esProvider.get());
        BinanceClient client = new BinanceClient(uri, handler, gson, this);
        clients.put(symbol, client);
      } catch (Exception ex) {
        LOGGER.error("Error creating websocket for symbol: " + sym, ex);
      }
    });
  }

  @Override
  public Collection<BinanceClient> get() {
    return clients.values();
  }

  @Override
  public void onConnected(String symbol) {
    LOGGER.info("Binance client[{}] connected", symbol);
  }

  @Override
  public void onDisconnected(String symbol) {
    if (!stopping) {
      LOGGER.info("Binance client[{}] disconnected, reconnecting...", symbol);
      clients.get(symbol).reconnect();
    }
  }

  @PreDestroy
  private void stop() {
    stopping = true;
  }
}
