package com.blokaly.ceres.kafka;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public class KafkaStreamModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  public KafkaStreams provideKafkaStreams(StreamsBuilder builder, Config config, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
    Properties props = new Properties();
    Config kafka = config.getConfig("kafka");
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafka.getString(StreamsConfig.APPLICATION_ID_CONFIG));
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    Topology topology = builder.build();
    KafkaStreams streams = new KafkaStreams(topology, props);
    streams.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    return streams;
  }

}
