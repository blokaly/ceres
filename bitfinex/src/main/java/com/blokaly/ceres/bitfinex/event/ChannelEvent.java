package com.blokaly.ceres.bitfinex.event;

public abstract class ChannelEvent extends AbstractEvent {

    public final int channelId;

    public ChannelEvent(int channelId, String type) {
        super(type);
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }
}
