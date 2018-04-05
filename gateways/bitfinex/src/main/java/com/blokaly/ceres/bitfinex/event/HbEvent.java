package com.blokaly.ceres.bitfinex.event;

public class HbEvent extends ChannelEvent {

    public HbEvent(int channelId) {
        super(channelId, "hb");
    }

    @Override
    public String toString() {
        return "HbEvent{" +
                "channelId=" + channelId +
                '}';
    }
}
