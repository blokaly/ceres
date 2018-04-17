package com.blokaly.ceres.bitfinex.event;

public class OrderBookEvent extends AbstractEvent {

    private final String channel = "book";
    private final String prec = "R0";
    private final String pair;

    public OrderBookEvent(String pair) {
        super("subscribe");
        this.pair = pair;
    }
}
