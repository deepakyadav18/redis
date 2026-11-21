package com.redis.storage;

public interface StorageEngine {

    void set(String key, String value);

    String get(String key);

    boolean delete(String key);
}
