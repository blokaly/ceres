package com.blokaly.ceres.bittrex;

import com.blokaly.ceres.bittrex.event.ExchangeDeltaEvent;
import com.blokaly.ceres.bittrex.event.ExchangeStateEvent;
import com.blokaly.ceres.common.PairSymbol;
import com.blokaly.ceres.kafka.ToBProducer;
import com.blokaly.ceres.orderbook.PriceAggregatedOrderBook;
import com.google.gson.Gson;
import com.google.inject.Provider;
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
    private final PairSymbol market;
    private final Provider<BittrexClient> provider;
    private final PriceAggregatedOrderBook orderBook;
    private final ToBProducer producer;
    private final Gson gson;
    private final BlockingQueue<ExchangeDeltaEvent> cache;
    private final ExecutorService executorService;
    private final Spliterator<ExchangeDeltaEvent> splitter;

    public OrderBookHandler(PairSymbol market, Provider<BittrexClient> provider, PriceAggregatedOrderBook orderBook, ToBProducer producer, Gson gson, ExecutorService executorService) {
        this.market = market;
        this.provider = provider;
        this.orderBook = orderBook;
        this.producer = producer;
        this.gson = gson;
        this.executorService = executorService;
        cache = new ArrayBlockingQueue<>(128);
        splitter = new QSpliterator<>(cache);
    }

    public PairSymbol getMarket() {
        return market;
    }

    public void start() {
        final String market = this.market.toString("-").toUpperCase();

        executorService.execute(() -> {
            StreamSupport.stream(splitter, false).forEach(event -> {
                if (orderBook.isInitialized()) {
                    orderBook.processIncrementalUpdate(event.getDeletion());
                    orderBook.processIncrementalUpdate(event.getUpdate());
                } else {
                    try {
                        ExchangeStateEvent snapshot = gson.fromJson(provider.get().requestSnapshot(market), ExchangeStateEvent.class);
                        while (snapshot.getSequence() <= event.getSequence()) {
                            Thread.sleep(1000L);
                            snapshot = gson.fromJson(provider.get().requestSnapshot(market), ExchangeStateEvent.class);
                        }
                        orderBook.processSnapshot(snapshot);
                    } catch (Exception ex) {
                        LOGGER.error("Error retrieving snapshot for " + orderBook.getSymbol(), ex);
                    }
                }
                producer.publish(orderBook);
            });
        });
        provider.get().subscribe(market);
    }

    public void handle(ExchangeDeltaEvent event) {
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
