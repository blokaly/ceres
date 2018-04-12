package com.blokaly.ceres.coinmarketcap;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.StringProducer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Exposed;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CoinMarketCapService extends BootstrapService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoinMarketCapService.class);
  private final TickerRequester requester;
  private final RefRateProducer producer;
  private final Gson gson;
  private final ScheduledExecutorService ses;
  private final Type tickersType;

  @Inject
  public CoinMarketCapService(TickerRequester requester, RefRateProducer producer, Gson gson, @SingleThread ScheduledExecutorService ses) {
    this.requester = requester;
    this.producer = producer;
    this.gson = gson;
    this.ses = ses;
    tickersType = new TypeToken<List<TickerEvent>>() {}.getType();
  }

  @Override
  protected void startUp() throws Exception {
    ses.scheduleAtFixedRate(()->{
      List<TickerEvent> tickers = gson.fromJson(requester.request(), tickersType);
      producer.publishRate(tickers);
    }, 0L, 5L, TimeUnit.MINUTES);

    awaitTerminated();
  }

  @Override
  protected void shutDown() throws Exception {

  }

  public static class CoinMarketCapModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      bindExpose(StringProducer.class);

      bindExpose(TickerRequester.class);
      bindExpose(RefRateProducer.class);
    }

    @Exposed
    @Provides
    @Singleton
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(TickerEvent.class, new TickerEventAdapter());
      return builder.create();
    }
  }

  public static void main(String[] args) {
    Services.start(new CoinMarketCapModule());
  }
}
