package com.blokaly.ceres.data;

import java.util.Collection;

public interface MarketDataIncremental<T extends OrderInfo> {

    enum Type {UNKNOWN, NEW, UPDATE, DONE;}

    Type type();

    long getSequence();

    Collection<T> orderInfos();

}
