package com.blokaly.ceres.redis;

public interface RedisClient {
  void set(String key, String value);
  void set(String key, String value, int timeout);
  String get(String key);
}
