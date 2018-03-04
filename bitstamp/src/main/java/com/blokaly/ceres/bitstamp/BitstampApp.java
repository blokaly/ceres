package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.bitstamp.event.OrderBookEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import com.pusher.client.Client;
import com.pusher.client.Pusher;

public class BitstampApp {

    public static class BitstampModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(MessageHandler.class).to(MessageHandlerImpl.class);
            bind(Service.class).to(PusherClient.class);
        }

        @Provides
        @Singleton
        public Client providePusherClient() {
            return new Pusher("de504dc5763aeef9ff52");
        }

        @Provides
        @Singleton
        public Gson provideGson() {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(OrderBookEvent.class, new OrderBookEventAdapter());
            builder.registerTypeAdapter(DiffBookEvent.class, new DiffBookEventAdapter());
            return builder.create();
        }

    }

    public static void main(String[] args) throws Exception {
        LifecycleInjector injector = InjectorBuilder
                .fromModules(new ShutdownHookModule(), new BitstampModule())
                .createInjector();
        injector.getInstance(Service.class).start();
        injector.awaitTermination();
    }
}
