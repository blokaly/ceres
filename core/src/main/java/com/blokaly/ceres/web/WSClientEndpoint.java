package com.blokaly.ceres.web;

import com.blokaly.ceres.data.FeedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.InputStream;

@ClientEndpoint
public class WSClientEndpoint {

    private static Logger LOGGER = LoggerFactory.getLogger(WSClientEndpoint.class);

   private final FeedHandler feedHandler;

    public WSClientEndpoint(FeedHandler feedHandler) {
        this.feedHandler = feedHandler;
    }

    @OnOpen
    public void onOpen(Session userSession) {
        LOGGER.info("Session open: {}", userSession.getId());
        feedHandler.sessionStatusChanged(userSession);
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOGGER.info("Session closed: {}", userSession.getId());
        feedHandler.sessionStatusChanged(userSession);
    }

    @OnMessage
    public void onMessage(InputStream stream) {
        LOGGER.debug("onMessage(stream)");
    }

    @OnMessage
    public void onMessage(String json) {
        LOGGER.debug("onMessage(string)");
        feedHandler.onMessage(json);
    }

    @OnError
    public void onError(Session userSession, Throwable error) {
        LOGGER.error("Session error: " + userSession.getId(), error);
    }

}
