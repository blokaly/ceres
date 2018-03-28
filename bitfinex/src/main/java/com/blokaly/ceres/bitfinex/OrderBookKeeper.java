package com.blokaly.ceres.bitfinex;

import com.blokaly.ceres.data.SymbolFormatter;
import com.blokaly.ceres.orderbook.OrderBasedOrderBook;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class OrderBookKeeper {

    private final Map<Integer, OrderBasedOrderBook> orderbooks;
    private final Map<Integer, String> symMap;
    private final List<String> symbols;
    private final String appName;

    @Inject
    public OrderBookKeeper(Config config) {
        symbols = config.getStringList("symbols");
        appName = config.getString("app.name");
        orderbooks = Maps.newHashMap();
        symMap = Maps.newHashMap();
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public String getSymbol(int channel) {
        return symMap.get(channel);
    }

    public void makeOrderBook(int channel, String symbol) {
        OrderBasedOrderBook book = orderbooks.get(channel);
        symbol = SymbolFormatter.normalise(symbol);
        String key = symbol + "." + appName;
        if (book == null) {
            book = new OrderBasedOrderBook(symbol, key);
            orderbooks.put(channel, book);
            symMap.put(channel, symbol);
        } else {
            book.clear();
            if (!book.getSymbol().equals(symbol)) {
                book = new OrderBasedOrderBook(symbol, key);
                orderbooks.put(channel, book);
            }
        }
    }

    public OrderBasedOrderBook get(int channel) {
        return orderbooks.get(channel);
    }
}
