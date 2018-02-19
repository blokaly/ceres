package com.blokaly.ceres.bitfinex.event;

public abstract class ChannelEvent extends AbstractEvent {

    protected final int channelId;

    public ChannelEvent(int channelId, String type) {
        super(type);
        this.channelId = channelId;
    }
}
