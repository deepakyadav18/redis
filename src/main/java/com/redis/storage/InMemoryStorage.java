package com.redis.storage;

import java.util.HashMap;

public class InMemoryStorage implements StorageEngine{

    private final HashMap<String, String> data = new HashMap<>();

    @Override
    public void set(String key, String value) {
        data.put(key, value);
    }

    @Override
    public String get(String key) {
        return data.get(key);
    }

    @Override
    public boolean delete(String key) {
        return data.remove(key) != null;
    }
}
