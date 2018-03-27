package com.blokaly.ceres.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class HttpReader implements AutoCloseable {

  private static final int OK = 200;
  private final HttpURLConnection conn;

  public HttpReader(HttpURLConnection conn) {
    this.conn = conn;
  }

  public HttpURLConnection getConnection() {
    return conn;
  }

  public String read() throws IOException {
    if (conn.getResponseCode() != OK) {
      throw new IOException("HTTP error code : " + conn.getResponseCode());
    }
    StringBuilder sb = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      in.lines().forEach(sb::append);
    }
    return sb.toString();
  }

  @Override
  public void close() throws Exception {
    if (conn != null) {
      conn.disconnect();
    }
  }
}
