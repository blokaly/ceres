package com.blokaly.ceres.coinmarketcap;

import com.blokaly.ceres.kafka.StringProducer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Predicate;

@Singleton
public class RefRateProducer {
  private static final Logger LOGGER = LoggerFactory.getLogger(RefRateProducer.class);
  private final StringProducer producer;

  @Inject
  public RefRateProducer(StringProducer producer) {
    this.producer = producer;
  }

  public void publishRate(Collection<TickerEvent> tickers) {
    tickers.parallelStream().filter(TickerEvent::isValid).forEach(evt -> {

    });
  }
}
