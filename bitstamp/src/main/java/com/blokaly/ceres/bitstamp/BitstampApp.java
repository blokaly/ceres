package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.bitstamp.event.OrderBookEvent;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.ExceptionLoggingHandler;
import com.google.common.util.concurrent.Service;
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
import com.typesafe.config.Config;

public class BitstampApp {

    public static class BitstampModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(MessageHandler.class).to(MessageHandlerImpl.class);
            bind(Service.class).to(PusherClient.class);
        }

        @Provides
        @Singleton
        public Client providePusherClient(Config config) {
            return new Pusher(config.getString("app.key"));
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
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionLoggingHandler());
        LifecycleInjector injector = InjectorBuilder
                .fromModules(new ShutdownHookModule(), new CommonModule(), new BitstampModule())
                .createInjector();
        injector.getInstance(Service.class).startAsync().awaitTerminated();
    }
}
