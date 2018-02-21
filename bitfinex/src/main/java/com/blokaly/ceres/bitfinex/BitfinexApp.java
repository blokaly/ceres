package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.callback.ChannelCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.CommandCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.InfoCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.SubscribedCallbackHandler;
import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;

import java.net.URI;
import java.util.Map;

public class BitfinexApp {

    public static class BitfinexModule extends AbstractModule {

        @Override
        protected void configure() {
            MapBinder<String, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), String.class, CommandCallbackHandler.class);
            binder.addBinding("info").to(InfoCallbackHandler.class);
            binder.addBinding("subscribed").to(SubscribedCallbackHandler.class);
            binder.addBinding("channel").to(ChannelCallbackHandler.class);
        }

        @Provides
        public URI provideUri() throws Exception {
            return new URI("wss://api.bitfinex.com/ws");
        }

        @Provides
        public MessageSender provideMessageSender(final BitfinexClient client) {
            return client::send;
        }

        @Provides
        @Singleton
        public Gson provideGson(Map<String, CommandCallbackHandler> handlers) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(AbstractEvent.class, new EventAdapter(handlers));
            return builder.create();
        }

        @Provides
        @Singleton
        public MessageHandler provideMessageHandler(Gson gson, MessageSender sender, OrderBookKeeper keeper) {
            return new MessageHandlerImpl(gson, sender, keeper);
        }

        @Provides
        public Service provideService(final BitfinexClient client) {
            return new Service() {
                @Override
                public void start() throws Exception {
                    client.connectBlocking();
                }

                @Override
                public void stop() {
                    client.close();
                }
            };
        }

    }

    public static void main(String[] args) throws Exception {
        LifecycleInjector injector = InjectorBuilder
                .fromModules(new ShutdownHookModule(), new BitfinexModule())
                .createInjector();
        Service service = injector.getInstance(Service.class);
        service.start();
    }
}
