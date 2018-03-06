package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BitstampKafkaProducer {

    private static Logger LOGGER = LoggerFactory.getLogger(BitstampKafkaProducer.class);
    private final Producer<String, String> producer;
    private final String topic;
    private final Gson gson;
    private volatile boolean closing = false;

    @Inject
    public BitstampKafkaProducer(Producer<String, String> producer, Gson gson, Config config) {
        this.producer = producer;
        this.gson = gson;
        topic = config.getString("kafka.topic");
    }

    @PreDestroy
    public void stop() {
        closing = true;
        producer.flush();
        producer.close();
    }

    public void publish(PriceBasedOrderBook orderBook) {

        if (closing) {
            return;
        }

        ArrayList<List<String[]>> tob = new ArrayList<>();
        tob.add(orderBook.topOfBids(1));
        tob.add(orderBook.topOfAsks(1));
        send(orderBook.getSymbol(), gson.toJson(tob));

    }

    private void send(String symbol, String message) {

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, symbol.toLowerCase(), message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.error("Error sending Kafka message", exception);
            }
        });
    }
}
