package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.bitstamp.event.OrderBookEvent;
import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.ExceptionLoggingHandler;
import com.blokaly.ceres.common.SingleThread;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.netflix.governator.InjectorBuilder;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BitstampApp extends AbstractService {

    private final List<PusherClient> clients;

    @Inject
    public BitstampApp(List<PusherClient> clients) {
        this.clients = clients;
    }

    @Override
    protected void doStart() {
        clients.forEach(PusherClient::start);
    }

    @Override
    @PreDestroy
    protected void doStop() {
        clients.forEach(PusherClient::stop);
    }

    public static class BitstampModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(MessageHandler.class).to(MessageHandlerImpl.class);
            bind(Service.class).to(BitstampApp.class);
        }

        @Provides
        @Singleton
        public List<PusherClient> providePusherClients(Config config, Gson gson, BitstampKafkaProducer producer, @SingleThread Provider<ExecutorService> provider) {
            PusherOptions options = new PusherOptions();

            return config.getConfig("symbols").entrySet().stream()
                    .map(item -> new PusherClient(item.getKey(), new Pusher((String)item.getValue().unwrapped(), options), producer, gson, provider.get()))
                    .collect(Collectors.toList());
        }

        @Provides
        @Singleton
        public Gson provideGson() {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(OrderBookEvent.class, new OrderBookEventAdapter());
            builder.registerTypeAdapter(DiffBookEvent.class, new DiffBookEventAdapter());
            return builder.create();
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
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionLoggingHandler());
        InjectorBuilder.fromModules(new ShutdownHookModule(), new CommonModule(), new BitstampModule())
                .createInjector()
                .getInstance(Service.class)
                .startAsync().awaitTerminated();
    }
}
