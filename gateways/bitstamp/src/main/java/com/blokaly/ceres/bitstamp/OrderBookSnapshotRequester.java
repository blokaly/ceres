package com.blokaly.ceres.bitstamp;

import com.blokaly.ceres.web.HttpReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OrderBookSnapshotRequester {
    private static Logger LOGGER = LoggerFactory.getLogger(OrderBookSnapshotRequester.class);
    private static final String ORDER_BOOK_URL = "https://www.bitstamp.net/api/v2/order_book/";
    private final URL url;

    public OrderBookSnapshotRequester(String symbol) {
        try {
            url = new URL(ORDER_BOOK_URL + symbol);
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
