package com.blokaly.ceres.bitfinex;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URI;

public class BitfinexClient extends WebSocketClient {

    private static Logger LOGGER = LoggerFactory.getLogger(BitfinexClient.class);
    private volatile boolean stop = false;
    private final JsonCracker cracker;
    private final ConnectionListener listener;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
    }

    public BitfinexClient(URI serverURI, JsonCracker cracker, ConnectionListener listener) {
        super(serverURI);
        this.cracker = cracker;
        this.listener = listener;
        LOGGER.info("client initiated");
    }



    @Override
    public void onOpen(ServerHandshake handshake) {
        LOGGER.info("ws open, status - {}:{}", handshake.getHttpStatus(), handshake.getHttpStatusMessage());
        if (listener != null) {
            listener.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        LOGGER.debug("ws message: {}", message);
        if (!stop) {
            cracker.crack(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("ws close: {}", reason);
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.error("ws error", ex);
    }

    @PreDestroy
    public void stop() {
        stop = true;
        super.close();
    }
}
