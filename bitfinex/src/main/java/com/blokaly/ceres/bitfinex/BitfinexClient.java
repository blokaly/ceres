package com.blokaly.ceres.bitfinex;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Singleton
public class BitfinexClient extends WebSocketClient implements Service {

    private static Logger LOGGER = LoggerFactory.getLogger(BitfinexClient.class);

    private JsonCracker cracker;

    @Inject
    public BitfinexClient(URI serverURI, JsonCracker cracker) {
        super(serverURI);
        this.cracker = cracker;
        LOGGER.info("client initiated");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("ws open, status: {}:{}", handshakedata.getHttpStatus(), handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        LOGGER.info("ws message: {}", message);
        cracker.crack(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("ws close: {}", reason);
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.error("ws error", ex);
    }

    @Override
    public void start() throws Exception {
        connect();
    }

    @Override
    public void stop() {
        close();
    }
}
