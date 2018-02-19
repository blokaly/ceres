package com.blokaly.ceres.data;

import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringWriter;

import static com.blokaly.ceres.data.MessageDecoder.Type.AUTH;
import static com.blokaly.ceres.data.MessageDecoder.Type.OB_SNAPSHOT;
import static com.blokaly.ceres.data.MessageDecoder.Type.PONG;

public class MessageEncoder {

    public static String pong() {
        StringWriter out = new StringWriter();
        try {
            JsonWriter writer = new JsonWriter(out);
            writer.beginObject();
            writer.name("e").value(PONG.getEString());
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static String authRequest(String key, String secret) {
        long timestamp = System.currentTimeMillis()/1000L;
        StringWriter out = new StringWriter();
        try {
            JsonWriter writer = new JsonWriter(out);
            writer.beginObject();
            writer.name("e").value(AUTH.getEString());
            writer.name("auth").beginObject()
                    .name("timestamp").value(timestamp)
                    .name("key").value(key)
                    .name("signature").value(getSignature(timestamp, key, secret))
                    .endObject();
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static String subscribe(String key, String pair, int depth) {
        StringWriter out = new StringWriter();
        try {
            JsonWriter writer = new JsonWriter(out);
            writer.beginObject();
            writer.name("e").value(OB_SNAPSHOT.getEString());
            writer.name("oid").value(key + "_order-book-subscribe");
            writer.name("data").beginObject()
                        .name("pair").beginArray()
                            .value(pair.substring(0, 3))
                            .value(pair.substring(3))
                        .endArray()
                        .name("subscribe").value(true)
                        .name("depth").value(depth)
                    .endObject();
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static String getSignature(long timestamp, String key, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String what = Long.toString(timestamp) + key;
            return Hex.encodeHexString(sha256_HMAC.doFinal(what.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
