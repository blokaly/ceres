package com.blokaly.ceres.data;

//import javax.websocket.Session;

public interface FeedHandler {
//    void sessionStatusChanged(Session session);
    void onMessage(String message);
}
