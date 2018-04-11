package com.blokaly.ceres.kraken;

import com.blokaly.ceres.common.SingleThread;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenDepth;
import org.knowm.xchange.kraken.service.KrakenMarketDataServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class MarketDataHandler {
  private static Logger LOGGER = LoggerFactory.getLogger(MarketDataHandler.class);
  private final KrakenMarketDataServiceRaw service;
  private final ScheduledExecutorService ses;
  private final OrderBookKeeper keeper;
  private final int depth;
  private final CountDownLatch latch;

  @Inject
  public MarketDataHandler(KrakenMarketDataServiceRaw service, @SingleThread ScheduledExecutorService ses, OrderBookKeeper orderBookKeeper, Config config) {
    this.service = service;
    this.ses = ses;
    keeper = orderBookKeeper;
    depth = config.getInt("depth");
    latch = new CountDownLatch(1);
  }

  public void start() {
    keeper.getAllSymbols().forEach(sym -> {
      ses.scheduleAtFixedRate(() -> {
        try {
          KrakenDepth orderbook = service.getKrakenDepth(new CurrencyPair(sym), depth);
          LOGGER.info("{}", orderbook);
          KrakenSnapshot snapshot = KrakenSnapshot.parse(orderbook);
          keeper.get(sym).processSnapshot(snapshot);
        } catch (IOException e) {
          LOGGER.error("Failed to retrieve depth", e);
        }
      }, 0L, 5, TimeUnit.SECONDS);
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    latch.countDown();
  }
}
