package com.redis.storage;

import java.util.List;

public interface StorageEngine {

    void set(String key, String value);

    String get(String key);

    int delete(List<String> key);
}
