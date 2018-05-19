package com.blokaly.ceres.simulation;

import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.kafka.GsonProducer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class RandomQuote {
  private static final Logger LOGGER = LoggerFactory.getLogger(RandomQuote.class);
  private final GsonProducer producer;
  private final ScheduledExecutorService ses;
  private final Random rand = new Random();
  private final String[] symbols = new String[]{"AAABBB", "BBBCCC"};
  private final String[] venues = new String[]{"FF1", "FF2", "FF3"};

  @Inject
  public RandomQuote(GsonProducer producer, @SingleThread ScheduledExecutorService ses) {
    this.producer = producer;
    this.ses = ses;
  }

  public void start() {
    ses.scheduleWithFixedDelay(this::run, 1L, 1, TimeUnit.SECONDS);
  }

  public void stop() {
    ses.shutdownNow();
  }

  private void run() {
    LOGGER.info("Publishing random quote");
    JsonObject json = new JsonObject();
    producer.publish("mock", generateQuote());
  }

  private JsonObject generateQuote() {
    JsonObject quote = new JsonObject();
    quote.addProperty("sym", symbols[rand.nextInt(symbols.length)]);
    quote.addProperty("venue", venues[rand.nextInt(venues.length)]);
    int depth = 5;
    quote.add("bids", getLadders(depth, false));
    quote.add("asks", getLadders(depth, true));
    return quote;
  }

  private JsonArray getLadders(int depth, boolean ascending) {
    JsonArray ladders = new JsonArray();
    int start = ascending ? depth + 1 : depth;
    if (rand.nextBoolean()) {
      for (int i = 0; i < depth; i++) {
        JsonArray ladder = new JsonArray();
        ladder.add((ascending ? start + i : start - i)  + rand.nextDouble());
        ladder.add(rand.nextDouble());
        ladders.add(ladder);
      }
    }
    return ladders;
  }
}
