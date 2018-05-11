package com.blokaly.ceres.binance;

import com.blokaly.ceres.web.HttpReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OrderBookSnapshotRequester {
    private static Logger LOGGER = LoggerFactory.getLogger(OrderBookSnapshotRequester.class);
    private static final String ORDER_BOOK_URL = "https://www.binance.com/api/v1/depth?symbol=%s&limit=10";
    private final URL url;

    public OrderBookSnapshotRequester(String symbol) {
        try {
            url = new URL(String.format(ORDER_BOOK_URL, symbol.toUpperCase()));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String request() {

        try (HttpReader reader = new HttpReader((HttpURLConnection)url.openConnection())) {
            HttpURLConnection conn = reader.getConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            return reader.read();
        } catch (Exception ex) {
            LOGGER.error("Error requesting snapshot", ex);
            return null;
        }
    }
}
