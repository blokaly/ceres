package com.blokaly.ceres.redis;

public interface RedisClient {
  void set(String key, String value);
  void set(String key, String value, int ttl);
  String get(String key);
}
