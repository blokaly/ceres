package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.binding.BootstrapService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.bitstamp.event.OrderBookEvent;
import com.blokaly.ceres.common.*;
import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.kafka.KafkaCommonModule;
import com.blokaly.ceres.kafka.KafkaStreamModule;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.*;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.typesafe.config.Config;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BitstampService extends BootstrapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BitstampService.class);
    private final List<PusherClient> clients;
    private final KafkaStreams streams;

    @Inject
    public BitstampService(List<PusherClient> clients, KafkaStreams streams) {
        this.clients = clients;
        this.streams = streams;
    }

    @Override
    protected void startUp() throws Exception {
        LOGGER.info("starting pusher client...");
        clients.forEach(PusherClient::start);
        LOGGER.info("starting kafka streams...");
        streams.start();
    }

    @Override
    protected void shutDown() throws Exception {
        LOGGER.info("stopping pusher client...");
        clients.forEach(PusherClient::stop);
        LOGGER.info("stopping kafka streams...");
        streams.close();
    }

    public static class BitstampModule extends CeresModule {

        @Override
        protected void configure() {
            install(new CommonModule());
            install(new KafkaCommonModule());
            install(new KafkaStreamModule());
            expose(KafkaStreams.class);
        }

        @Provides
        @Singleton
        @Exposed
        public List<PusherClient> providePusherClients(Config config, Gson gson, ToBProducer producer, @SingleThread Provider<ExecutorService> provider) {
            PusherOptions options = new PusherOptions();
            String exchange = Exchange.valueOf(config.getString("app.exchange").toUpperCase()).getCode();
            return config.getConfig("symbols").entrySet().stream()
                .map(item -> {
                    String symbol = SymbolFormatter.normalise(item.getKey());
                    OrderBookHandler handler = new OrderBookHandler(new PriceBasedOrderBook(symbol, symbol + "." + exchange), producer, gson, provider.get());
                    String subId = (String) item.getValue().unwrapped();
                    return new PusherClient(new Pusher(subId, options), handler, gson);
                })
                .collect(Collectors.toList());
        }

        @Provides
        @Singleton
        public StreamsBuilder provideStreamsBuilder(Config config) {
            String topic = config.getString(CommonConfigs.KAFKA_TOPIC);
            int windowSecond = config.getInt(CommonConfigs.KAFKA_THROTTLE_SECOND);
            StreamsBuilder builder = new StreamsBuilder();
            builder.stream(topic)
                .groupByKey()
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(windowSecond)))
                .reduce((agg, v)->v)
                .toStream((k,v)->k.key())
                .to(topic + ".throttled");
            return builder;
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

    public static void main(String[] args) {
        Services.start(new BitstampModule());
    }
}
