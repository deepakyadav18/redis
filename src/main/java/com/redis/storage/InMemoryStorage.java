package com.redis.storage;

import java.util.HashMap;
import java.util.List;

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
    public int delete(List<String> keys) {
        int deletedCount = 0;
        for(String key: keys) {
            if(data.remove(key) != null) deletedCount++;
        }
        return deletedCount;
    }
}
