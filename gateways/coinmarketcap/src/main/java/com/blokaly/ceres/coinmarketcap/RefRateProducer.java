package com.blokaly.ceres.coinmarketcap;

import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.Source;
import com.blokaly.ceres.kafka.StringProducer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@Singleton
public class RefRateProducer {
  private static final Logger LOGGER = LoggerFactory.getLogger(RefRateProducer.class);
  private static final String USD = "USD";
  private final StringProducer producer;
  private final String source;
  private List<TickerEvent> tickers;

  @Inject
  public RefRateProducer(Config config, StringProducer producer) {
    this.producer = producer;
    source = Source.getCode(config, CommonConfigs.APP_SOURCE);
    tickers = Collections.emptyList();
  }

  public void publishRate() {
    tickers.parallelStream().filter(TickerEvent::isValid).forEach(evt -> {
      String key = getKey(evt);
      String price = evt.getUsdPrice().toString();
      LOGGER.debug("refrate: {}:{}", key, price);
      producer.publish(key, price);
    });
  }

  private String getKey(TickerEvent evt) {
    String symbol = (evt.getSymbol() + USD).toLowerCase();
    if (source != null) {
      return symbol + "." + source;
    } else {
      return symbol;
    }
  }

  public void update(List<TickerEvent> tickers) {
    this.tickers = tickers;
  }
}
