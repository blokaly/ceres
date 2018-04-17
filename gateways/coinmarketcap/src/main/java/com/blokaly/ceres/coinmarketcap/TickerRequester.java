package com.blokaly.ceres.coinmarketcap;

import com.blokaly.ceres.web.HttpReader;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class TickerRequester {
  private static Logger LOGGER = LoggerFactory.getLogger(TickerRequester.class);
  private static final String COINMARKETCAP_TICKER_API = "https://api.coinmarketcap.com/v1/ticker/?limit=0";
  private final URL url;

  public TickerRequester() {
    try {
      url = new URL(COINMARKETCAP_TICKER_API);
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
