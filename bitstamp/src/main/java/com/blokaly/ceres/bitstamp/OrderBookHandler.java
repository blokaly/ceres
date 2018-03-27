package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.bitstamp.event.OrderBookEvent;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class OrderBookHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBookHandler.class);
    private final PriceBasedOrderBook orderBook;
    private final ToBProducer producer;
    private final Gson gson;
    private final BlockingQueue<DiffBookEvent> cache;
    private final ExecutorService executorService;
    private final Spliterator<DiffBookEvent> splitter;

    public OrderBookHandler(PriceBasedOrderBook orderBook, ToBProducer producer, Gson gson, ExecutorService executorService) {
        this.orderBook = orderBook;
        this.producer = producer;
        this.gson = gson;
        this.executorService = executorService;
        cache = new ArrayBlockingQueue<>(128);
        splitter = new QSpliterator<>(cache);
    }

    public String getSymbol() {
        return orderBook.getSymbol();
    }

    public void start() {
        executorService.execute(() -> {
            StreamSupport.stream(splitter, false).forEach(event -> {
                    if (orderBook.isInitialized()) {
                        orderBook.processIncrementalUpdate(event.getDeletion());
                        orderBook.processIncrementalUpdate(event.getUpdate());
                    } else {
                        OrderBookSnapshotRequester requester = new OrderBookSnapshotRequester(orderBook.getSymbol());
                        OrderBookEvent snapshot = gson.fromJson(requester.request(), OrderBookEvent.class);
                        while (snapshot.getSequence() <= event.getSequence()) {
                            try {
                                Thread.sleep(1000L);
                                snapshot = gson.fromJson(requester.request(), OrderBookEvent.class);
                            } catch (InterruptedException e) {
                                if (Thread.currentThread().isInterrupted()) {
                                    LOGGER.info("Retrieving snapshot interrupted, quitting...");
                                    break;
                                }
                            }
                        }
                        orderBook.processSnapshot(snapshot);
                    }
                producer.publish(orderBook);
                });
        });
    }

    public void handle(DiffBookEvent event) {
        cache.add(event);
    }


    private static final class QSpliterator<T> implements Spliterator<T> {

        private final BlockingQueue<T> queue;

        private QSpliterator(BlockingQueue<T> queue) {
            this.queue = queue;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            try {
                action.accept(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }


        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return  Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED;
        }

    }
}
