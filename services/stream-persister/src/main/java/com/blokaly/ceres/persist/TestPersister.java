package com.blokaly.ceres.persist;

import com.blokaly.ceres.common.SingleThread;
import com.blokaly.ceres.influxdb.InfluxdbWriter;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class TestPersister extends AbstractIdleService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestPersister.class);
  private final InfluxdbWriter writer;
  private final Gson gson;
  private final ScheduledExecutorService executor;
  private final Map<String, String> tags;
  private final Random rand = new Random();
  private volatile boolean stop = false;

  @Inject
  public TestPersister(InfluxDB influxDB, Gson gson, @SingleThread ScheduledExecutorService executor) {
    writer = new InfluxdbWriter(influxDB, "test");
    this.gson = gson;
    this.executor = executor;
    tags = Maps.newHashMap();
    tags.put("venue", "finfabrik");
    tags.put("symbol", "ethusd");
  }


  @Override
  protected void startUp() throws Exception {
    LOGGER.info("starting up");
    executor.scheduleWithFixedDelay(this::persist, 1L, 1L, TimeUnit.SECONDS);
  }

  @Override
  protected void shutDown() throws Exception {
    LOGGER.info("shutting down");
    stop = true;
  }

  private void persist() {
    if (!stop) {
      Point.Builder builder = Point.measurement("stream").time(System.nanoTime(), TimeUnit.NANOSECONDS).tag(tags);
      boolean hasValue = false;
      String bids = getLadders();
      if (bids != null) {
        builder.addField("bids", bids);
        hasValue = true;
      }
      String asks = getLadders();
      if (asks != null) {
        builder.addField("asks", asks);
        hasValue = true;
      }

      if (!hasValue) {
        return;
      }

      try {
        LOGGER.info("Persisting ladders to influxdb");
        writer.write(builder.build());
        LOGGER.info("Persisted");
      } catch (Exception ex) {
        LOGGER.error("Error writing to influxdb", ex);
      }
    }
  }

  private String getLadders() {
    if (rand.nextBoolean()) {
      JsonArray ladders = new JsonArray();
      for (int i = 0; i < 10; i++) {
        JsonArray ladder = new JsonArray();
        ladder.add(rand.nextDouble());
        ladder.add(rand.nextDouble());
        ladders.add(ladder);
      }
      return gson.toJson(ladders);
    } else {
      return null;
    }
  }
}
