package com.blokaly.ceres.bitfinex.event;

public class SubscribedEvent extends AbstractEvent {
    private String channel;
    private int chanId;
    private String prec;
    private String freq;
    private String len;
    private String pair;

    @Override
    public String toString() {
        return "SubscribedEvent{" +
                "channel=" + channel +
                ", chanId=" + chanId +
                ", prec=" + prec +
                ", freq=" + freq +
                ", len=" + len +
                ", pair=" + pair +
                '}';
    }
}
