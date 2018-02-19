package com.blokaly.ceres.bitfinex.event;

public class InfoEvent extends AbstractEvent {
    private String version;

    @Override
    public String toString() {
        return "InfoEvent{" +
                "version=" + version +
                '}';
    }
}
