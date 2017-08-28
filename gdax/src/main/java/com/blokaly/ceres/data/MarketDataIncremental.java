package com.blokaly.ceres.data;

public interface MarketDataIncremental {

    enum Type {UNKNOWN, NEW, UPDATE, DONE;}

    Type type();

    long getSequence();

    OrderInfo orderInfo();

}
