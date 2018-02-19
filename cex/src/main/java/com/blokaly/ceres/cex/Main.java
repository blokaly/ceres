package com.blokaly.ceres.cex;

import com.blokaly.ceres.concurrent.DisruptorBuilder;
import com.blokaly.ceres.data.CexFeedHandler;
import com.blokaly.ceres.orderbook.CexOrderBook;
import com.blokaly.ceres.proto.OrderBookProto;
import com.blokaly.ceres.web.WSClientEndpoint;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {

        EventHandler<OrderBookProto.OrderBookMessage.Builder> handler = (event, sequence, endOfBatch) -> {
            if (event.getBidsCount()>0 && event.getAsksCount()>0) {
                OrderBookProto.Level bid = event.getBids(0);
                OrderBookProto.Level ask = event.getAsks(0);
                LOGGER.info("Top of Book: [{},{}] [{},{}]", bid.getPrice(), bid.getSize(), ask.getPrice(), ask.getSize());
            }
        };

        Disruptor<OrderBookProto.OrderBookMessage.Builder> disruptor = DisruptorBuilder.createDisruptor("OrderBookSnapshot",
                OrderBookProto.OrderBookMessage::newBuilder, 512,
                ProducerType.SINGLE,
                new BlockingWaitStrategy(),
                handler);

        disruptor.start();
        RingBuffer<OrderBookProto.OrderBookMessage.Builder> ringBuffer = disruptor.getRingBuffer();

        String symbol = "BTCUSD";
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        CexOrderBook book = new CexOrderBook(symbol, 1);
        final WSClientEndpoint clientEndPoint = new WSClientEndpoint(new CexFeedHandler(executor, book));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(clientEndPoint, new URI("wss://ws.cex.io/ws/"));

        executor.scheduleAtFixedRate(() -> {
            ringBuffer.publishEvent(book);
        }, 1, 1, TimeUnit.SECONDS);
    }
}
