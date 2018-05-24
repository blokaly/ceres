package com.blokaly.ceres.influxdb;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.ThreadFactory;

@Singleton
public class InfluxdbProvider implements Provider<InfluxDB> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxdbProvider.class);
    private final String url;
    private final String username;
    private final String password;
    private InfluxDB influxDB;
    private ThreadFactory threadFactory;

    @Inject
    public InfluxdbProvider(Config config, ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        Config influxdbConf = config.getConfig("influxdb");
        url = influxdbConf.getString("url");
        username = influxdbConf.getString("username");
        password = influxdbConf.getString("password");
    }

    @Override
    public synchronized InfluxDB get() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(url, username, password);
            BatchOptions batchOptions = BatchOptions.DEFAULTS.threadFactory(threadFactory).exceptionHandler((points, throwable) -> LOGGER.error("Failed to write influxdb points", throwable));
            influxDB.enableBatch(batchOptions);
            influxDB.enableGzip();
        }
        return influxDB;
    }

    @PreDestroy
    public synchronized void close() {
        if (influxDB != null) {
            influxDB.close();
        }
    }
}
