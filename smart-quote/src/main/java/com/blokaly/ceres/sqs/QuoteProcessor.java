package com.blokaly.ceres.sqs;

import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.sqs.event.SimpleOrderBook;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Singleton
public class QuoteProcessor {
  private static Logger LOGGER = LoggerFactory.getLogger(QuoteProcessor.class);
  private final Consumer<String, String> kafkaConsumer;
  private final Gson gson;
  private final List<String> topics;
  private final ExecutorService executorService;
  private volatile boolean stop;

  @Inject
  public QuoteProcessor(Consumer<String, String> kafkaConsumer, Config config, Gson gson, @SingleThread Provider<ExecutorService> provider) {
    this.kafkaConsumer = kafkaConsumer;
    this.gson = gson;
    Config kafka = config.getConfig("kafka");
    this.topics = kafka.getStringList("topics");
    executorService = provider.get();
  }

  public void start() {
    stop = false;
    executorService.execute(() -> {
      kafkaConsumer.subscribe(topics);
      waitAndProcess();
      kafkaConsumer.unsubscribe();
      kafkaConsumer.close();
    });
  }

  private void waitAndProcess() {
    while (!stop) {
      try {
        ConsumerRecords<String, String> records = kafkaConsumer.poll(Long.MAX_VALUE);
        kafkaConsumer.commitSync(); // “at most once” delivery
        for (ConsumerRecord<String, String> record : records) {
          SimpleOrderBook orderBook = gson.fromJson(record.value(), SimpleOrderBook.class);
          orderBook.setSymbol(record.key());
          orderBook.setSequence(record.timestamp());
          LOGGER.info("{}", orderBook);
        }
      } catch (WakeupException e) {
        // ignore
      }
    }
  }

  public void stop() {
    stop = true;
    kafkaConsumer.wakeup();
  }
}
