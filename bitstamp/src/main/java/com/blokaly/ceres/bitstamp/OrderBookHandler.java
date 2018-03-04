package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.bitstamp.event.DiffBookEvent;
import com.blokaly.ceres.bitstamp.event.OrderBookEvent;
import com.blokaly.ceres.orderbook.PriceBasedOrderBook;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class OrderBookHandler {

    private final PriceBasedOrderBook orderBook;
    private final Gson gson;
    private final BlockingQueue<DiffBookEvent> cache;
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private final Spliterator<DiffBookEvent> splitter;

    public OrderBookHandler(PriceBasedOrderBook orderBook, Gson gson) {
        this.orderBook = orderBook;
        this.gson = gson;
        cache = new ArrayBlockingQueue<>(128);
        splitter = new QSpliterator<>(cache);
    }

    public void start() {
        es.execute(() -> {
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
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            snapshot = gson.fromJson(requester.request(), OrderBookEvent.class);
                        }
                        orderBook.processSnapshot(snapshot);
                    }

                System.out.println(Arrays.toString(orderBook.topOfBids(1).get(0)));
                System.out.println(Arrays.toString(orderBook.topOfAsks(1).get(0)));
                });
        });
    }

    public void handle(DiffBookEvent event) {
        cache.add(event);
    }


    private static final class QSpliterator<T> implements Spliterator<T> {

        private final BlockingQueue<T> queue;

        public QSpliterator(BlockingQueue<T> queue) {
            this.queue = queue;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            try {
                action.accept(queue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Take interrupted.", e);
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
