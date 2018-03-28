package com.blokaly.ceres.bitfinex.event;

public class InfoEvent extends AbstractEvent {
    private String version;

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "InfoEvent{" +
                "version=" + version +
                '}';
    }
}
