package com.blokaly.ceres.bitstamp;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageHandlerImpl implements MessageHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private final Gson gson;

    @Inject
    public MessageHandlerImpl(Gson gson) {
        this.gson = gson;
    }
}
