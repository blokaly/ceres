package com.blokaly.ceres.persist;

import com.blokaly.ceres.binding.AwaitExecutionService;
import com.blokaly.ceres.binding.CeresModule;
import com.blokaly.ceres.common.Services;
import com.blokaly.ceres.influxdb.InfluxdbModule;
import com.google.inject.Inject;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamPersistService extends AwaitExecutionService {
    private static Logger LOGGER = LoggerFactory.getLogger(StreamPersistService.class);
    private InfluxDB influxDB;
    private final TestPersister testPersister;

    @Inject
    public StreamPersistService(InfluxDB influxDB, TestPersister testPersister) {
        this.influxDB = influxDB;
        this.testPersister = testPersister;
    }

    @Override
    protected void startUp() throws Exception {
        LOGGER.info("Stream persister starting...");
        Pong pong = influxDB.ping();
        if (pong.isGood()) {
            LOGGER.info("Influxdb connection OK");
        }
        testPersister.startUp();
    }

    @Override
    protected void shutDown() throws Exception {
        LOGGER.info("Stream persister stopping...");
        testPersister.shutDown();
    }

    private static class StreamPersistModule extends CeresModule {

        @Override
        protected void configure() {
            install(new InfluxdbModule());
            expose(InfluxDB.class);
        }
    }

    public static void main(String[] args) {
        Services.start(new StreamPersistModule());
    }
}