package com.blokaly.ceres.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

public class InfluxdbWriter {

    private InfluxDB influxDB;
    private String database;
    private String rpName;

    public InfluxdbWriter(InfluxDB influxDB, String database) {
        this(influxDB, database, null);
    }

    public InfluxdbWriter(InfluxDB influxDB, String database, String rpName) {
        this.influxDB = influxDB;
        this.database = database;
        this.rpName = rpName;
    }

    public void write(Point point) {
        influxDB.write(database, rpName, point);
    }
}
