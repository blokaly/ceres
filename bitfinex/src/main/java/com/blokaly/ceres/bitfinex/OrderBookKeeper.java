package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

    private final Map<Integer, OrderBasedOrderBook> orderbooks;

    public OrderBookKeeper() {
        orderbooks = new HashMap<>();
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
