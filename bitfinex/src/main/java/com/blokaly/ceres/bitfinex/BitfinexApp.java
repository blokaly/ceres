package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.callback.*;
import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.bitfinex.event.EventType;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import com.netflix.governator.InjectorBuilder;
import com.typesafe.config.Config;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Map;

import static com.blokaly.ceres.bitfinex.event.EventType.*;

public class BitfinexApp extends AbstractService {

    private final Provider<BitfinexClient> provider;

    @Inject
    public BitfinexApp(BitfinexClientProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void doStart() {
        provider.get().connect();
    }

    @Override
    @PreDestroy
    protected void doStop() {
        provider.get().close();
    }

    public static class BitfinexModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new CommonModule());
            install(new KafkaCommonModule());
            MapBinder<EventType, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), EventType.class, CommandCallbackHandler.class);
            binder.addBinding(INFO).to(InfoCallbackHandler.class);
            binder.addBinding(SUBSCRIBED).to(SubscribedCallbackHandler.class);
            binder.addBinding(CHANNEL).to(ChannelCallbackHandler.class);
            binder.addBinding(PING).to(PingPongCallbackHandler.class);
            binder.addBinding(PONG).to(PingPongCallbackHandler.class);

            bind(BitfinexClient.class).toProvider(BitfinexClientProvider.class).in(Singleton.class);
            bind(MessageHandler.class).to(MessageHandlerImpl.class).in(Singleton.class);
            bind(Service.class).to(BitfinexApp.class);
        }

        @Provides
        public URI provideUri(Config config) throws Exception {
            return new URI(config.getString("app.ws.url"));
        }

        @Provides
        @Singleton
        public Gson provideGson(Map<EventType, CommandCallbackHandler> handlers) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(AbstractEvent.class, new EventAdapter(handlers));
            return builder.create();
        }
    }

    public static void main(String[] args) throws Exception {
        InjectorBuilder.fromModules( new BitfinexModule())
                .createInjector()
                .getInstance(Service.class)
                .startAsync().awaitTerminated();
    }
}
