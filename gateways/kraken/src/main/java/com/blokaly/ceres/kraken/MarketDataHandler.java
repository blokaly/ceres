package com.blokaly.ceres.kraken;

import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenDepth;
import org.knowm.xchange.kraken.service.KrakenMarketDataServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class MarketDataHandler {
  private static Logger LOGGER = LoggerFactory.getLogger(MarketDataHandler.class);
  private final KrakenMarketDataServiceRaw service;
  private final ScheduledExecutorService ses;
  private final OrderBookKeeper keeper;
  private final ToBProducer producer;
  private final int depth;

  @Inject
  public MarketDataHandler(KrakenMarketDataServiceRaw service,
                           @SingleThread ScheduledExecutorService ses,
                           OrderBookKeeper orderBookKeeper,
                           ToBProducer producer,
                           Config config) {
    this.service = service;
    this.ses = ses;
    keeper = orderBookKeeper;
    this.producer = producer;
    depth = config.getInt("depth");
  }

  public void start() {
    int idx = 0;
    for (String sym : keeper.getAllSymbols()) {
      ses.schedule(() -> { pullMarketData(sym); }, ++idx * 3, TimeUnit.SECONDS);
    }
  }

  private void pullMarketData(String sym) {
    try {
      KrakenDepth orderbook = service.getKrakenDepth(new CurrencyPair(sym), depth);
      LOGGER.debug("{}", orderbook);
      KrakenSnapshot snapshot = KrakenSnapshot.parse(orderbook);
      DepthBasedOrderBook orderBook = keeper.get(sym);
      orderBook.processSnapshot(snapshot);
      producer.publish(orderBook);
    } catch (Exception e) {
      LOGGER.error("Failed to retrieve depth", e);
    } finally {
      ses.schedule(() -> {pullMarketData(sym);}, 3, TimeUnit.SECONDS);
    }
  }

  public void stop() { }
}
