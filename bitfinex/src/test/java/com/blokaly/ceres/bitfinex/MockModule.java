package com.blokaly.ceres.bitfinex;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockModule extends AbstractModule {

    private static Logger LOGGER = LoggerFactory.getLogger(MockModule.class);

    @Override
    protected void configure() {
        bind(OutgoingMessageQueue.class);
    }

    @Provides
    public AbstractService provideService() {
        return new AbstractService() {
            @Override
            protected void doStart() {
                LOGGER.info("Mock service started");
            }

            @Override
            protected void doStop() {
                LOGGER.info("Mock service stopped");
            }
        };
    }
}
