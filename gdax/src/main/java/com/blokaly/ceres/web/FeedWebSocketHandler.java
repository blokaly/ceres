package com.blokaly.ceres.web;

import com.blokaly.ceres.proto.OrderBookProto;
import com.google.common.collect.Sets;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@WebSocket
public class FeedWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedWebSocketHandler.class);

    private final Set<Session> sessions = Sets.newConcurrentHashSet();

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        sessions.add(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    public void broadcast(OrderBookProto.OrderBookMessage message) {
        sessions.forEach(session->{
            try {
                session.getRemote().sendBytes(message.toByteString().asReadOnlyByteBuffer());
            } catch (IOException e) {
                LOGGER.error("Failed to send order book over ws", e);
            }
        });
    }
}
