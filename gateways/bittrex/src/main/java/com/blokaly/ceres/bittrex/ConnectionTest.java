package com.blokaly.ceres.bittrex;

import com.blokaly.ceres.common.GzipUtils;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

import java.io.IOException;
import java.util.Base64;

public class ConnectionTest {

  public static final String MARKET = "USDT-BTC";

  public static void main(String[] args) throws Exception {
    HubConnection connection = new HubConnection("https://socket.bittrex.com", null, true, new SignalRLogger());
    HubProxy proxy = connection.createHubProxy("c2");
    proxy.on("uE", message -> {
      try {
        byte[] decoded = Base64.getDecoder().decode(message);
        String data = GzipUtils.inflateRaw(decoded);
        System.out.println("delta: " + data);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }, String.class);
    connection.start().get();
    proxy.invoke("SubscribeToExchangeDeltas", MARKET);
    String encoded = proxy.invoke(String.class, "QueryExchangeState", MARKET).get();
    byte[] decoded = Base64.getDecoder().decode(encoded);
    String data = GzipUtils.inflateRaw(decoded);
    System.out.println("snapshot: " + data);
  }
}
