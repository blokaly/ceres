package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Singleton
public class BitfinexKafkaProducer {

    private static Logger LOGGER = LoggerFactory.getLogger(BitfinexKafkaProducer.class);
    private final Producer<String, String> producer;
    private final Gson gson;
    private volatile boolean closing = false;

    @Inject
    public BitfinexKafkaProducer(Gson gson) {
        this.gson = gson;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "BitfinexProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }

    @PreDestroy
    public void stop() {
        closing = true;
        producer.flush();
        producer.close();
    }

    public void publish(OrderBasedOrderBook orderBook) {

        if (closing) {
            return;
        }

        ArrayList<List<String[]>> tob = new ArrayList<>();
        tob.add(orderBook.topOfBids(1));
        tob.add(orderBook.topOfAsks(1));
        send(orderBook.getSymbol(), gson.toJson(tob));

    }

    private void send(String symbol, String message) {

        ProducerRecord<String, String> record = new ProducerRecord<>("md.bitfinex", symbol.toLowerCase(), message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.error("Error sending Kafka message", exception);
            }
        });
    }
}
