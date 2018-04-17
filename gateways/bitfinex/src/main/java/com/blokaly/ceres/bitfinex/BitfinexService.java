package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.bitfinex.callback.*;
import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.bitfinex.event.EventType;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.kafka.HBProducer;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Exposed;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static com.blokaly.ceres.bitfinex.event.EventType.*;

public class BitfinexService extends BootstrapService {
  private final BitfinexClient client;
  private final KafkaStreams streams;

  @Inject
  public BitfinexService(BitfinexClient client, @Named("Throttled") KafkaStreams streams) {
    this.client = client;
    this.streams = streams;
  }

  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting websocket client...");
    client.connect();
    waitFor(3);
    LOGGER.info("starting kafka streams...");
    streams.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("stopping websocket client...");
    client.close();
    LOGGER.info("stopping kafka streams...");
    streams.close();
  }

  public static class BitfinexModule extends CeresModule {

    @Override
    protected void configure() {
      install(new KafkaCommonModule());
      install(new KafkaStreamModule());
      bindExpose(ToBProducer.class);
      bind(HBProducer.class).asEagerSingleton();
      expose(StreamsBuilder.class).annotatedWith(Names.named("Throttled"));
      expose(KafkaStreams.class).annotatedWith(Names.named("Throttled"));

      bindAllCallbacks();
      bindExpose(MessageHandler.class).to(MessageHandlerImpl.class).in(Singleton.class);
      bindExpose(BitfinexClient.class).toProvider(BitfinexClientProvider.class).in(Singleton.class);
    }

    @Provides
    @Exposed
    public URI provideUri(Config config) throws Exception {
      return new URI(config.getString("app.ws.url"));
    }

    @Provides
    @Singleton
    @Exposed
    public Gson provideGson(Map<EventType, CommandCallbackHandler> handlers) {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(AbstractEvent.class, new EventAdapter(handlers));
      return builder.create();
    }

    private void bindAllCallbacks() {
      MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
      binder.addBinding(INFO).to(InfoCallbackHandler.class);
      binder.addBinding(SUBSCRIBED).to(SubscribedCallbackHandler.class);
      binder.addBinding(CHANNEL).to(ChannelCallbackHandler.class);
      binder.addBinding(PING).to(PingPongCallbackHandler.class);
      binder.addBinding(PONG).to(PingPongCallbackHandler.class);
    }
  }

  public static void main(String[] args) {
    Services.start(new BitfinexModule());
  }
}
