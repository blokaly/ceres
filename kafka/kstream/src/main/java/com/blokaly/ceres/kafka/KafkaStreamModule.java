package com.blokaly.ceres.kafka;

import com.blokaly.ceres.common.CommonConfigs;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaStreamModule extends AbstractModule {
  @Override
  protected void configure() { }

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

  @Provides
  @Singleton
  @Named("Throttled")
  public KafkaStreams provideThrottledKafkaStreams(@Named("Throttled") StreamsBuilder builder, Config config, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
    Properties props = new Properties();
    Config kafka = config.getConfig("kafka");
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafka.getString(StreamsConfig.APPLICATION_ID_CONFIG));
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "1024");
    Topology topology = builder.build();
    KafkaStreams streams = new KafkaStreams(topology, props);
    streams.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    return streams;
  }

  @Provides
  @Singleton
  @Named("Throttled")
  public StreamsBuilder provideStreamsBuilder(Config config) {
    Logger streamLogger = LoggerFactory.getLogger(KafkaStreams.class);
    StreamsBuilder builder = new StreamsBuilder();
    String topic = config.getString(CommonConfigs.KAFKA_TOPIC);
    int windowSecond = config.getInt(CommonConfigs.KAFKA_THROTTLE_SECOND);
    if (windowSecond > 0) {
      builder.stream(topic)
          .groupByKey()
          .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(windowSecond)))
          .reduce((agg, v)->{
            streamLogger.debug("reduce: {}->{}", agg, v);
            return v;
          })
          .toStream((k,v)->k.key())
          .to(topic + ".throttled");
    } else {
      builder.stream(topic).to(topic + ".throttled");
    }
    return builder;
  }
}
