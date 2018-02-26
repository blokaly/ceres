package com.blokaly.ceres.bitfinex;

import com.google.inject.Singleton;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Properties;

@Singleton
public class BitfinexKafkaProducer {

    private static Logger LOGGER = LoggerFactory.getLogger(BitfinexKafkaProducer.class);
    private Producer<String, String> producer;
    private volatile boolean closing = false;

    public BitfinexKafkaProducer() {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "BitfinexProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }


    public void send(String message) {
        if (closing) {
            return;
        }
        ProducerRecord<String, String> record = new ProducerRecord<>("bitfinex", "BTCUSD", message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.error("Error sending Kafka message", exception);
            }
        });
    }


    @PreDestroy
    public void stop() {
        closing = true;
        producer.flush();
        producer.close();
    }
}
