package com.blokaly.ceres.influxdb;

import com.blokaly.ceres.binding.CeresModule;
import com.google.inject.Singleton;
import org.influxdb.InfluxDB;

public class InfluxdbModule extends CeresModule {

    @Override
    protected void configure() {
        bindExpose(InfluxdbProvider.class);
        bind(InfluxDB.class).toProvider(InfluxdbProvider.class).in(Singleton.class);
        expose(InfluxDB.class);
    }
}
