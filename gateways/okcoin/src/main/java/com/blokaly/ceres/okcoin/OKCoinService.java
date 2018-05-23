package com.blokaly.ceres.okcoin;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.CommonConfigs;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.common.Source;
import com.blokaly.ceres.kafka.HBProducer;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.okcoin.event.ChannelEvent;
import com.blokaly.ceres.okcoin.event.EventAdapter;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OKCoinService extends BootstrapService {
  private final Provider<OKCoinClient> provider;
  private final KafkaStreams streams;

  @Inject
  public OKCoinService(Provider<OKCoinClient> provider, @Named("Throttled") KafkaStreams streams) {
    this.provider = provider;
    this.streams = streams;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting OKCoin client...");
    provider.get().connect();

    waitFor(3);
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping OKCoin client...");
    provider.get().close();
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class OKCoinModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      install(new KafkaStreamModule());
      bindExpose(ToBProducer.class);
      bind(HBProducer.class).asEagerSingleton();
      expose(StreamsBuilder.class).annotatedWith(Names.named("Throttled"));
      expose(KafkaStreams.class).annotatedWith(Names.named("Throttled"));

      bind(MessageHandler.class).to(MessageHandlerImpl.class).in(Singleton.class);
      bindExpose(OKCoinClient.class).toProvider(OKCoinClientProvider.class).in(Singleton.class);
    }

    @Provides
    @Exposed
    public URI provideUri(Config config) throws Exception {
      return new URI(config.getString("app.ws.url"));
    }

    @Provides
    @Singleton
    @Exposed
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(ChannelEvent.class, new EventAdapter());
      return builder.create();
    }

    @Provides
    @Singleton
    @Exposed
    public Map<String, PriceBasedOrderBook> provideOrderBooks(Config config) {
      List<String> channels = config.getStringList("channels");
      String source = Source.valueOf(config.getString(CommonConfigs.APP_SOURCE).toUpperCase()).getCode();
      String channelPattern = "ok_sub_spot_([a-z]+)_([a-z]+)_depth";
      Pattern pattern = Pattern.compile(channelPattern);

      return channels.stream().collect(Collectors.<String , String, PriceBasedOrderBook>toMap(chan->chan, chan -> {
        Matcher matcher = pattern.matcher(chan);
        if (matcher.matches()) {
          String pair = matcher.group(1) + matcher.group(2);
          return new PriceBasedOrderBook(pair, pair + "." + source);
        } else {
          throw new IllegalArgumentException("channel pattern is wrong: " + chan);
        }
      }));
    }
  }

  public static void main(String[] args) {
    Services.start(new OKCoinModule());
  }
}
