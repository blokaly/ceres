package com.blokaly.ceres.anx;

import com.blokaly.ceres.common.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;

@Singleton
public class AnxRestClient {

  private static Logger LOGGER = LoggerFactory.getLogger(AnxRestClient.class);
  private static final String HMAC_SHA512 = "HmacSHA512";

  private final String host;
  private final String path;
  private final String key;
  private final String secret;
  private final Gson gson;

  @Inject
  public AnxRestClient(Config config, Gson gson) {
    this.gson = gson;
    Config apiConfig = config.getConfig("api");
    host = apiConfig.getString("host");
    path = apiConfig.getString("path.token");
    key = apiConfig.getString("key");
    secret = apiConfig.getString("secret");
  }

  public Pair<String, String> getUuidAndToken() {

    @SuppressWarnings("unchecked")
    Pair<String, String> rtn = (Pair<String, String>) Pair.NULL_PAIR;
    HttpsURLConnection conn = null;
    try {
      String postData = preparePayload();
      String signed = sign(secret, postData);
      URL url = new URL(host + path);
      conn = (HttpsURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; ANX node.js client)");
      conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Rest-Key", key);
      conn.setRequestProperty("Rest-Sign", signed);

      LOGGER.info("requesting token");
      // Send post request
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
      conn.getOutputStream().write(postData.getBytes("UTF8"));

      if (conn.getResponseCode() != 200) {
        LOGGER.error("Failed to retrieve uuid and token from anx, response:{}", conn.getResponseCode());
      } else {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
          in.lines().forEach(sb::append);
        }
        JsonObject result = new JsonParser().parse(sb.toString()).getAsJsonObject();
        if ("OK".equalsIgnoreCase(result.get("resultCode").getAsString())) {
          LOGGER.info("received token");
          String token = result.get("token").getAsString();
          String uuid = result.get("uuid").getAsString();
          return new Pair<>(uuid, token);
        }
      }
    } catch (Exception iex) {
      LOGGER.error("Failed to retrieve uuid and token from anx", iex);
      System.exit(1);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }

    return rtn;
  }

  private String preparePayload() {
    JsonObject jsonObject = new JsonObject();
    long nonce = System.currentTimeMillis() * 1000;
    jsonObject.addProperty("tonce", nonce);
    return gson.toJson(jsonObject);
  }

  private String sign(String secret, String postData) throws Exception {
    String message = path + "\0" + postData;
    SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secret.getBytes()), HMAC_SHA512);
    Mac mac = Mac.getInstance(HMAC_SHA512);
    mac.init(secretKeySpec);
    byte[] bytes = mac.doFinal(message.getBytes());
    return Base64.getEncoder().encodeToString(bytes).trim();
  }
}
