package com.blokaly.ceres.bitfinex;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.*;
import com.google.inject.Singleton;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Singleton
public class OutgoingMessageQueue {

    private final Deque<String> messageQueue;

    public OutgoingMessageQueue() {
        this.messageQueue = new LinkedList<>();
    }

    public void send(String message) {
        messageQueue.addLast(message);
    }

    public boolean messageReceived(String message) {
        JsonParser parser = new JsonParser();
        JsonElement expected = parser.parse(message);

        return waitUtil(() ->
            messageQueue.stream().filter(received ->
                    compareJson(expected, parser.parse(received))).findFirst().isPresent()
        );
    }

    private boolean waitUtil(Supplier<Boolean> condition) {
        int wait = 0;
        while (true) {
            if (condition.get()) {
                return true;
            } else if (wait >= 2000L) {
                return false;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException ignored) {}
            wait += 200L;
        }
    }

    private static boolean compareJson(JsonElement json1, JsonElement json2) {
        boolean isEqual = true;
        // Check whether both jsonElement are not null
        if(json1 !=null && json2 !=null) {

            // Check whether both jsonElement are objects
            if (json1.isJsonObject() && json2.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> ens1 = ((JsonObject) json1).entrySet();
                Set<Map.Entry<String, JsonElement>> ens2 = ((JsonObject) json2).entrySet();
                JsonObject json2obj = (JsonObject) json2;
                if (ens1 != null && ens2 != null && (ens2.size() == ens1.size())) {
                    // Iterate JSON Elements with Key values
                    for (Map.Entry<String, JsonElement> en : ens1) {
                        isEqual = isEqual && compareJson(en.getValue() , json2obj.get(en.getKey()));
                    }
                } else {
                    return false;
                }
            }

            // Check whether both jsonElement are arrays
            else if (json1.isJsonArray() && json2.isJsonArray()) {
                JsonArray jarr1 = json1.getAsJsonArray();
                JsonArray jarr2 = json2.getAsJsonArray();
                if(jarr1.size() != jarr2.size()) {
                    return false;
                } else {
                    int i = 0;
                    // Iterate JSON Array to JSON Elements
                    for (JsonElement je : jarr1) {
                        isEqual = isEqual && compareJson(je , jarr2.get(i));
                        i++;
                    }
                }
            }

            // Check whether both jsonElement are null
            else if (json1.isJsonNull() && json2.isJsonNull()) {
                return true;
            }

            // Check whether both jsonElement are primitives
            else if (json1.isJsonPrimitive() && json2.isJsonPrimitive()) {
                if(json1.equals(json2)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if(json1 == null && json2 == null) {
            return true;
        } else {
            return false;
        }
        return isEqual;
    }
}
