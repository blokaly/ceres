package com.blokaly.ceres.bitfinex.event;

public abstract class AbstractEvent {

    protected String event;

    AbstractEvent() {}

    AbstractEvent(String event) {
        this.event = event;
    }
}
