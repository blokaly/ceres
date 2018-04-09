package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.binding.CeresService;
import com.blokaly.ceres.bitfinex.callback.*;
import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.bitfinex.event.EventType;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import com.typesafe.config.Config;

import java.net.URI;
import java.util.Map;

import static com.blokaly.ceres.bitfinex.event.EventType.*;

public class BitfinexService extends BootstrapService {

    private final Provider<BitfinexClient> provider;

    @Inject
    public BitfinexService(BitfinexClientProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void startUp() throws Exception {
        provider.get().connect();
    }

    @Override
    protected void shutDown() throws Exception {
        provider.get().close();
    }

    public static class BitfinexModule extends CeresModule {

        @Override
        protected void configure() {
            install(new CommonModule());
            install(new KafkaCommonModule());
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
