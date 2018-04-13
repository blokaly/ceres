package com.blokaly.ceres.kraken;

import com.blokaly.ceres.common.CommonConfigs;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

@Singleton
public class KrakenRefService {

  private final KafkaStreams streams;

  @Inject
  public KrakenRefService(Config config) {
    String topic = config.getString(CommonConfigs.KAFKA_TOPIC);
    StreamsBuilder builder = new StreamsBuilder();
    builder.stream(topic).to("md.refrate");
    Properties props = new Properties();
    Config kafka = config.getConfig("kafka");
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "RefRateProducer");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "1024");
    Topology topology = builder.build();
    streams = new KafkaStreams(topology, props);
  }

  @PostConstruct
  public void start() {
    streams.start();
  }

  @PreDestroy
  public void stop() {
    streams.close();
  }
}
