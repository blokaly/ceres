package com.blokaly.ceres.sqs;

import com.blokaly.ceres.common.CommonModule;
import com.blokaly.ceres.common.DumpAndShutdownModule;
import com.blokaly.ceres.sqs.event.SimpleOrderBook;
import com.blokaly.ceres.sqs.event.SimpleOrderBookAdapter;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.governator.InjectorBuilder;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.annotation.PreDestroy;
import java.util.Properties;

public class SmartQuoteApp extends AbstractService {

  private final QuoteProcessor processor;

  @Inject
  public SmartQuoteApp(QuoteProcessor processor) {
    this.processor = processor;
  }

  @Override
  protected void doStart() {
    processor.start();
  }

  @Override
  @PreDestroy
  protected void doStop() {
    processor.stop();
  }

  public static class SQSModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Service.class).to(SmartQuoteApp.class);
    }

    @Provides
    @Singleton
    public Consumer<String, String> provideKafakaProducer(Config config) {
      Config kafka = config.getConfig("kafka");
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getString(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
      props.put(ConsumerConfig.GROUP_ID_CONFIG, kafka.getString(ConsumerConfig.GROUP_ID_CONFIG));
      props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafka.getInt(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
      return new KafkaConsumer<>(props);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(SimpleOrderBook.class, new SimpleOrderBookAdapter());
      return builder.create();
    }
  }

  public static void main(String[] args) throws Exception {
    InjectorBuilder.fromModules(new DumpAndShutdownModule(), new CommonModule(), new SQSModule())
        .createInjector()
        .getInstance(Service.class)
        .startAsync().awaitTerminated();
  }
}
