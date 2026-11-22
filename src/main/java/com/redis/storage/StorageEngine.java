package com.redis.storage;

import java.util.List;

public interface StorageEngine {

    void set(String key, String value);

    String get(String key);

    int delete(List<String> key);

    boolean expire(String key, long seconds);

    long ttl(String key);

    int activeExpire(int sampleSize);

    long incrementBy(String key, long delta);

    long lpush(String key, List<String> values);

    long rpush(String key, List<String> values);

    String lpop(String key);

    String rpop(String key);

    long llen(String key);

    List<String> lrange(String key, long start, long stop);

    String lindex(String key, long index);

}
