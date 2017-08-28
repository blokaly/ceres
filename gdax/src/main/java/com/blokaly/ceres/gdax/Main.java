package com.blokaly.ceres.gdax;

import com.blokaly.ceres.data.FeedHandler;
import com.blokaly.ceres.disruptor.DisruptorBuilder;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.blokaly.ceres.proto.OrderBookProto;
import com.blokaly.ceres.web.CorsFilter;
import com.blokaly.ceres.web.FeedWebSocketHandler;
import com.blokaly.ceres.web.WSClientEndpoint;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        FeedWebSocketHandler feedWebSocketHandler = new FeedWebSocketHandler();
        EventHandler<OrderBookProto.OrderBookMessage.Builder> handler = (event, sequence, endOfBatch) -> {
            OrderBookProto.Level bid = event.getBids(0);
            OrderBookProto.Level ask = event.getAsks(0);
            LOGGER.info("Top of Book: [{},{}] [{},{}]", bid.getPrice(), bid.getSize(), ask.getPrice(), ask.getSize());
//            feedWebSocketHandler.broadcast(event.build());
        };

        Disruptor<OrderBookProto.OrderBookMessage.Builder> disruptor = DisruptorBuilder.createDisruptor("OrderBookSnapshot",
                OrderBookProto.OrderBookMessage::newBuilder, 512,
                ProducerType.SINGLE,
                new BlockingWaitStrategy(),
                handler);

        disruptor.start();
        RingBuffer<OrderBookProto.OrderBookMessage.Builder> ringBuffer = disruptor.getRingBuffer();

        Service service = Service.ignite().port(4567);
        service.webSocket("/orderbook", feedWebSocketHandler);
        service.get("/orderbook", (req, res) -> {
            res.status(200);
            return null;
        });
        CorsFilter.apply(service);
        service.awaitInitialization();

        String symbol = "BTC-USD";
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        OrderBasedOrderBook book = new OrderBasedOrderBook(symbol);
        final WSClientEndpoint clientEndPoint = new WSClientEndpoint(new FeedHandler(executor, book));
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(clientEndPoint, new URI("wss://ws-feed.gdax.com"));

        executor.scheduleAtFixedRate(() -> {
            ringBuffer.publishEvent(book);
        }, 1, 1, TimeUnit.SECONDS);


    }
}
