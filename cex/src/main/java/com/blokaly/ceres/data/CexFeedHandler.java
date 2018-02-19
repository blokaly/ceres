package com.blokaly.ceres.data;

import com.blokaly.ceres.cex.CexMDIncremental;
import com.blokaly.ceres.cex.CexMDSnapshot;
import com.blokaly.ceres.orderbook.CexOrderBook;
import com.blokaly.ceres.orderbook.DepthBasedOrderBook;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.concurrent.ExecutorService;

public class CexFeedHandler implements FeedHandler{
    private static final Logger LOGGER = LoggerFactory.getLogger(CexFeedHandler.class);
    private final ExecutorService executor;
    private final CexOrderBook orderBook;
    private volatile Session currentSession;

    public CexFeedHandler(ExecutorService executor, CexOrderBook orderBook) {
        this.executor = executor;
        this.orderBook = orderBook;
    }

    public void sessionStatusChanged(Session session) {
        if (currentSession == null) {
            if (session.isOpen()) {
                currentSession = session;
                authRequest();
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

    private void reset() {
        currentSession = null;
    }

    public void onMessage(String message) {

        MessageDecoder.CexMessage cexMessage = MessageDecoder.crack(message);

        switch (cexMessage.type) {
            case CONNECTED:
                LOGGER.info("connected");
                break;
            case PING:
                pong();
                break;
            case AUTH:
                if (MessageDecoder.isAuthOk(cexMessage)) {
                    LOGGER.info("auth ok");
                    subscribe();
                } else {
                    LOGGER.error("auth failed: {}", message);
                }
                break;
            case OB_SNAPSHOT:
                LOGGER.info("{}", message);
                CexMDSnapshot snapshot = CexMDSnapshot.parse(cexMessage);
                orderBook.processSnapshot(snapshot);
                break;
            case MD_UPDATE:
                LOGGER.info("{}", message);
                CexMDIncremental mdIncremental = CexMDIncremental.parse(cexMessage);
                orderBook.processIncrementalUpdate(mdIncremental);
                break;
            default:
        }
    }

    private void pong() {
        executor.submit(()->{
            currentSession.getAsyncRemote().sendText(MessageEncoder.pong());
        });
    }

    private void authRequest() {
        executor.submit(()->{
            String auth = MessageEncoder.authRequest("yjppYOves3Y13alzK9LYiEC2KsA", "hDnrQ4BZytpL9lU4VvtUIBjc");
            LOGGER.info("Requesting auth {}", auth);
            currentSession.getAsyncRemote().sendText(auth);
        });
    }

    private void subscribe() {
        executor.submit(() -> {
            String subscribe = MessageEncoder.subscribe("yjppYOves3Y13alzK9LYiEC2KsA", "BTCUSD", orderBook.getDepth());
            LOGGER.info("Subscribing {}", subscribe);
            currentSession.getAsyncRemote().sendText(subscribe);
        });
    }


}
