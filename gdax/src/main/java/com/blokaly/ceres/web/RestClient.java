package com.blokaly.ceres.web;

import com.blokaly.ceres.gdax.GdaxMDSnapshot;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
    private static final String DEFAULT_HOST = "api.gdax.com";

    public static GdaxMDSnapshot orderBookSnapshot(String symbol) throws Exception {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(DEFAULT_HOST)
                    .setPath("/products/" + symbol + "/book")
                    .setParameter("level", "3")
                    .build();
            HttpGet httpget = new HttpGet(uri);
            LOGGER.info("Executing request {}", httpget.getRequestLine());

            String obJson =  httpclient.<String>execute(httpget, response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    LOGGER.error("Unexpected response status: {}", status);
                    return null;
                }
            });

            return GdaxMDSnapshot.parse(obJson);
        }
    }
}
