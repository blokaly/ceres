package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.callback.ChannelCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.CommandCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.InfoCallbackHandler;
import com.blokaly.ceres.bitfinex.callback.SubscribedCallbackHandler;
import com.blokaly.ceres.bitfinex.event.AbstractEvent;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.DumpAndShutdownModule;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.netflix.governator.InjectorBuilder;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

public class BitfinexApp extends AbstractService {

    private final BitfinexClient client;

    @Inject
    public BitfinexApp(BitfinexClient client) {
        this.client = client;
    }

    @Override
    protected void doStart() {
        client.connect();
    }

    @Override
    protected void doStop() {
        client.close();
    }

    public static class BitfinexModule extends AbstractModule {

        @Override
        protected void configure() {
            MapBinder<String, CommandCallbackHandler> binder = MapBinder.newMapBinder(binder(), String.class, CommandCallbackHandler.class);
            binder.addBinding("info").to(InfoCallbackHandler.class);
            binder.addBinding("subscribed").to(SubscribedCallbackHandler.class);
            binder.addBinding("channel").to(ChannelCallbackHandler.class);
            bind(Service.class).to(BitfinexApp.class);
        }

        @Provides
        public URI provideUri(Config config) throws Exception {
            return new URI(config.getString("app.ws.url"));
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
        public MessageHandler provideMessageHandler(Gson gson, MessageSender sender, OrderBookKeeper keeper, BitfinexKafkaProducer producer) {
            return new MessageHandlerImpl(gson, sender, keeper, producer);
        }

        @Provides
        @Singleton
        public Producer<String, String> provideKafakaProducer(Config config) {
            Config kafka = config.getConfig("kafka");
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            props.put(ProducerConfig.CLIENT_ID_CONFIG, kafka.getString(ProducerConfig.CLIENT_ID_CONFIG));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            return new KafkaProducer<>(props);
        }
    }

    public static void main(String[] args) throws Exception {
        InjectorBuilder.fromModules(new DumpAndShutdownModule(), new CommonModule(), new BitfinexModule())
                .createInjector()
                .getInstance(Service.class)
                .startAsync().awaitTerminated();
    }
}
