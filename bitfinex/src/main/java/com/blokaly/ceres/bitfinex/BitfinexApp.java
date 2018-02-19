package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.callback.ChannelCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.CommandCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.InfoCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.SubscribedCallbackHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import org.java_websocket.WebSocket;

import java.net.URI;

public class BitfinexApp {

    private static class BitfinexModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Service.class).to(BitfinexClient.class);
            bind(WebSocket.class).to(BitfinexClient.class);
            MapBinder<String, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), String.class, CommandCallbackHandler.class);
            binder.addBinding("info").to(InfoCallbackHandler.class);
            binder.addBinding("subscribed").to(SubscribedCallbackHandler.class);
            binder.addBinding("channel").to(ChannelCallbackHandler.class);
        }

        @Provides
        public URI provideUri() throws Exception {
            return new URI("wss://api.bitfinex.com/ws");
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
