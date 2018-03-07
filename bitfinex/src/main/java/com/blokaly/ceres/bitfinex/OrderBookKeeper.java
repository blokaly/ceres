package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

    private final Map<Integer, OrderBasedOrderBook> orderbooks;
    private final List<String> symbols;

    @Inject
    public OrderBookKeeper(Config config) {
        symbols = config.getStringList("symbols");
        orderbooks = new HashMap<>();
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public OrderBasedOrderBook makeOrderBook(int channel, String symbol) {
        OrderBasedOrderBook book = orderbooks.get(channel);
        if (book == null) {
            book = new OrderBasedOrderBook(symbol);
            orderbooks.put(channel, book);
        } else {
            book.clear();
            if (!book.getSymbol().equals(symbol)) {
                book = new OrderBasedOrderBook(symbol);
                orderbooks.put(channel, book);
            }
        }
        return book;
    }

    public OrderBasedOrderBook get(int channel) {
        return orderbooks.get(channel);
    }
}
