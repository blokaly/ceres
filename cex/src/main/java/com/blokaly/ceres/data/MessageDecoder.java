package com.blokaly.ceres.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MessageDecoder {

    enum Type {
        CONNECTED("connected"), PING("ping"), PONG("pong"), AUTH("auth"), OB_SNAPSHOT("order-book-subscribe"), MD_UPDATE("md_update");
        private final String e;

        private Type(String e) {
            this.e = e;
        }

        public String getEString() {
            return e;
        }

        public static Type lookupByEString(String value) {
            for(Type type : values()){
                if( type.e.equals(value)){
                    return type;
                }
            }
            return null;
        }

    }

    public static class CexMessage {
        public final Type type;
        public final JsonObject data;

        private CexMessage(Type type, JsonObject data) {
            this.type = type;
            this.data = data;
        }
    }

    public static CexMessage crack(String response) {
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        String eString = jsonObject.get("e").getAsString();
        Type type = Type.lookupByEString(eString);
        if (type == null) {
            throw new IllegalArgumentException("Unknown response: " + response);
        }

        JsonElement elm = jsonObject.get("data");
        return new CexMessage(type, elm==null ?  null : elm.getAsJsonObject());
    }

    public static boolean isAuthOk(CexMessage message) {
        return "ok".equals(message.data.get("ok").getAsString());
    }
}
