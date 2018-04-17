package com.blokaly.ceres.quote;

import com.blokaly.ceres.common.SingleThread;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class HBProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(HBProcessor.class);
  private final ToBMapper mapper;
  private final ScheduledExecutorService ses;
  private final ConcurrentMap<String, Long> hbCache;

  @Inject
  public HBProcessor(ToBMapper mapper, @SingleThread ScheduledExecutorService ses) {
    this.mapper = mapper;
    this.ses = ses;
    hbCache = Maps.newConcurrentMap();
  }

  @PostConstruct
  public void start() {
    ses.scheduleAtFixedRate(this::check, 5L, 5, TimeUnit.SECONDS);
  }

  public void process(String key, String value) {
    hbCache.put(key, Long.valueOf(value));
  }

  private void check() {
    final long now = System.currentTimeMillis();
    List<String> staled = hbCache.keySet().stream().filter(producer -> {
      long timestamp = hbCache.get(producer);
      return now - timestamp > 5000L && hbCache.remove(producer, timestamp);
    }).map(key -> key.substring(3)).collect(Collectors.toList());

    if (!staled.isEmpty()) {
      LOGGER.info("hb lost for {}", staled.toArray());
      mapper.remove(staled);
    }
  }
}
