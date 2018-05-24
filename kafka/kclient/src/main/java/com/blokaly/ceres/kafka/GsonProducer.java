package com.blokaly.ceres.kafka;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.apache.kafka.clients.producer.Producer;

@Singleton
public class GsonProducer extends StringProducer {

  private final Gson gson;

  @Inject
  public GsonProducer(Producer<String, String> producer, Config config, Gson gson) {
    super(producer, config);
    this.gson = gson;
  }

  public void publish(String key, JsonObject json) {
    if (json != null) {
      super.publish(key, gson.toJson(json));
    }
  }
}
