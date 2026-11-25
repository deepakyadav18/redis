package com.redis.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RedisDatabase {

    private final Map<String, RedisObject> data = new HashMap<>();
    private final Map<String, Long> expires = new HashMap<>();

    public void put(String key, RedisObject object) {
        data.put(key, object);
        expires.remove(key);
    }

    public void putPreservingExpiry(String key, RedisObject object) {
        data.put(key, object);
    }

    public RedisObject get(String key) {
        if (isExpired(key)) {
            remove(key);
            return null;
        }

        return data.get(key);
    }

    public RedisObject remove(String key) {
        boolean expired = isExpired(key);

        RedisObject object = data.remove(key);
        expires.remove(key);

        return expired ? null : object;
    }

    public boolean containsKey(String key) {
        if (isExpired(key)) {
            remove(key);
            return false;
        }

        return data.containsKey(key);
    }

    private boolean isExpired(String key) {
        Long expiryTime = expires.get(key);

        return expiryTime != null &&
                System.currentTimeMillis() >= expiryTime;
    }

    public boolean expire(String key, long seconds) {
        if (!containsKey(key)) {
            return false;
        }

        if (seconds <= 0) {
            remove(key);
            return true;
        }

        try {
            long expirationMillis = Math.multiplyExact(seconds, 1000L);
            long expiryTime = Math.addExact(
                    System.currentTimeMillis(),
                    expirationMillis
            );

            expires.put(key, expiryTime);
            return true;

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Expiration time is too large", e);
        }
    }

    public long ttl(String key) {
        if (!containsKey(key)) {
            return -2;
        }

        Long expiryTime = expires.get(key);

        if (expiryTime == null) {
            return -1;
        }

        long remainingMillis = expiryTime - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            remove(key);
            return -2;
        }

        return remainingMillis / 1000;
    }

    public int activeExpire(int sampleSize) {
        if (sampleSize <= 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        int checked = 0;
        int expired = 0;

        Iterator<Map.Entry<String, Long>> iterator =
                expires.entrySet().iterator();

        while (iterator.hasNext() && checked < sampleSize) {
            Map.Entry<String, Long> entry = iterator.next();

            checked++;

            if (currentTime >= entry.getValue()) {
                data.remove(entry.getKey());
                iterator.remove();
                expired++;
            }
        }

        return expired;
    }

    public Map<String, RedisObject> getData() {
        return data;
    }

    public Map<String, Long> getExpires() {
        return expires;
    }
}