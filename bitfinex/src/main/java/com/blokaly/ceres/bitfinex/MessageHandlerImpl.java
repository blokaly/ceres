package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.bitfinex.event.InfoEvent;
import com.blokaly.ceres.bitfinex.event.OrderBookEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageHandlerImpl implements MessageHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerImpl.class);
    private final Gson gson;
    private final MessageSender sender;

    @Inject
    public MessageHandlerImpl(Gson gson, MessageSender sender) {
        this.gson = gson;
        this.sender = sender;
    }

    @Override
    public void onMessage(InfoEvent event) {
        String jsonString = gson.toJson(new OrderBookEvent("BTCUSD"));
        LOGGER.info("subscribe: {}", jsonString);
        sender.send(jsonString);
    }
}
