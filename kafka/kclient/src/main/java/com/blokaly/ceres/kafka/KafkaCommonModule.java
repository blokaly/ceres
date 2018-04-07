package com.blokaly.ceres.kafka;

import com.blokaly.ceres.common.Configs;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaCommonModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ToBProducer.class).in(Singleton.class);
    bind(TextProducer.class).in(Singleton.class);
    bind(HBProducer.class).asEagerSingleton();
  }

  @Provides
  @Singleton
  public Producer<String, String> provideKafkaProducer(Config config) {
    Config kafka = config.getConfig("kafka");
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    props.put(ProducerConfig.CLIENT_ID_CONFIG, kafka.getString(ProducerConfig.CLIENT_ID_CONFIG));
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    return new KafkaProducer<>(props);
  }

  @Provides
  @Singleton
  public Consumer<String, String> provideKafkaConsumer(Config config) {
    Config kafka = config.getConfig("kafka");
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafka.getString(ConsumerConfig.GROUP_ID_CONFIG));
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, Configs.getOrDefault(kafka, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, Configs.STRING_EXTRACTOR, CommonConfigs.DEFAULT_CONSUMER_GROUP_TIMEOUT));
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    return new KafkaConsumer<String, String>(props);
  }
}
