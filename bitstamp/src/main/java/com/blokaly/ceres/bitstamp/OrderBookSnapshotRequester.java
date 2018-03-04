package com.blokaly.ceres.bitstamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OrderBookSnapshotRequester {

    private final URL url;

    public OrderBookSnapshotRequester(String symbol) {
        try {
            url = new URL("https://www.bitstamp.net/api/v2/order_book/" + symbol);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String request() {

        try (JsonReader reader = new JsonReader()) {
            reader.open();
            return reader.read();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private class JsonReader implements AutoCloseable {

        private HttpURLConnection conn;
        private BufferedReader reader;

        private void open() throws IOException {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
        }

        private String read() throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return sb.toString();
        }

        @Override
        public void close() throws Exception {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
