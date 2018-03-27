package com.blokaly.ceres.kafka;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ToBProducer.class).in(Singleton.class);
    bind(HBProducer.class).asEagerSingleton();
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
