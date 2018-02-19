package com.blokaly.ceres.data;

import com.blokaly.ceres.gdax.GdaxMDIncremental;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.Session;
import java.util.Base64;
import java.util.concurrent.ExecutorService;

public class GdaxFeedHandler implements FeedHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GdaxFeedHandler.class);
    private final ExecutorService executor;
    private final JsonObj jsonObj;
    private final OrderBasedOrderBook orderBook;
    private volatile Session currentSession;

    public GdaxFeedHandler(ExecutorService executor, OrderBasedOrderBook book) {
        this.executor = executor;
        this.orderBook = book;
        jsonObj = new JsonObj(new String[]{book.getSymbol()});
    }

    public void sessionStatusChanged(Session session) {
        if (currentSession == null) {
            if (session.isOpen()) {
                currentSession = session;
                subscribe();
            }
        } else {
            if (currentSession != session) {
                throw new IllegalArgumentException("Unknown session" + session);
            }
            if (!session.isOpen()) {
                reset();
            }
        }
    }

    private void subscribe() {
        executor.submit(()->{
            String subString = new Gson().toJson(jsonObj);
            LOGGER.info("Subscribing {}", subString);
            currentSession.getAsyncRemote().sendText(subString);
        });
    }

    private void reset() {
        currentSession = null;
    }

    public void onMessage(String message) {
        GdaxMDIncremental marketData = GdaxMDIncremental.parse(message);
        executor.submit(()-> orderBook.processIncrementalUpdate(marketData));
    }

    public static String getSignature(String timestamp, String secret) {
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(decoded, "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String what = timestamp + "GET" + "/users/self";
            byte[] encoded = Base64.getEncoder().encode(sha256_HMAC.doFinal(what.getBytes()));
            return new String(encoded);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class JsonObj {
        private final String type = "subscribe";
        private final String[] product_ids;

        private JsonObj(String[] product_ids) {
            this.product_ids = product_ids;
        }
    }
}
