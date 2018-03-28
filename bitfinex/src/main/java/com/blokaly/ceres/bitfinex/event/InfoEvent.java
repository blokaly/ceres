package com.blokaly.ceres.bitfinex.event;

public class InfoEvent extends AbstractEvent {
    private String version;
    private String code;
    private String msg;

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "InfoEvent{" +
                "version=" + version +
                "code=" + code +
                "msg=" + msg +
                '}';
    }
}
