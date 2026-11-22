
package com.redis.storage;

import java.util.*;

public class InMemoryStorage implements StorageEngine {

    private final HashMap<String, String> data = new HashMap<>();
    private final HashMap<String, Long> expiry = new HashMap<>();
    private final HashMap<String, Deque<String>> lists = new HashMap<>();

    @Override
    public void set(String key, String value) {
        lists.remove(key);
        data.put(key, value);
        expiry.remove(key);
    }

    @Override
    public String get(String key) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (lists.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        return data.get(key);
    }

    @Override
    public int delete(List<String> keys) {
        int deletedCount = 0;

        for (String key : keys) {
            if (isExpired(key)) {
                removeExpiredKey(key);
                continue;
            }

            boolean deleted = data.remove(key) != null;
            deleted |= lists.remove(key) != null;

            if (deleted) {
                expiry.remove(key);
                deletedCount++;
            }
        }

        return deletedCount;
    }

    @Override
    public boolean expire(String key, long seconds) {
        if (isExpired(key)) {
            removeExpiredKey(key);
            return false;
        }

        if (!data.containsKey(key) && !lists.containsKey(key)) {
            return false;
        }

        if (seconds <= 0) {
            removeExpiredKey(key);
            return true;
        }

        try {
            long expiryMillis = Math.multiplyExact(seconds, 1000L);
            long expiryTime = Math.addExact(
                    System.currentTimeMillis(), expiryMillis
            );

            expiry.put(key, expiryTime);
            return true;

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("ERR invalid expire time");
        }
    }

    @Override
    public long ttl(String key) {
        if (!data.containsKey(key) && !lists.containsKey(key)) {
            return -2;
        }

        if (!expiry.containsKey(key)) {
            return -1;
        }

        long expiryTime = expiry.get(key);
        long currentTime = System.currentTimeMillis();

        if (expiryTime <= currentTime) {
            removeExpiredKey(key);
            return -2;
        }

        return (expiryTime - currentTime) / 1000;
    }

    @Override
    public int activeExpire(int sampleSize) {
        int expiredCount = 0;
        int sampledCount = 0;

        Iterator<Map.Entry<String, Long>> iterator =
                expiry.entrySet().iterator();

        long currentTime = System.currentTimeMillis();

        while (iterator.hasNext() && sampledCount < sampleSize) {
            Map.Entry<String, Long> entry = iterator.next();
            sampledCount++;

            if (currentTime >= entry.getValue()) {
                String key = entry.getKey();

                data.remove(key);
                lists.remove(key);
                iterator.remove();

                expiredCount++;
            }
        }

        return expiredCount;
    }

    @Override
    public long incrementBy(String key, long delta) throws NumberFormatException {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (lists.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        String currentValue = data.get(key);

        if (currentValue == null) {
            data.put(key, String.valueOf(delta));
            return delta;
        }

        try {
            long value = Long.parseLong(currentValue);
            long incrementedValue = Math.addExact(value, delta);

            data.put(key, String.valueOf(incrementedValue));
            return incrementedValue;

        } catch (NumberFormatException e) {
            throw new NumberFormatException("ERR Value is not an integer");

        } catch (ArithmeticException e) {
            throw new ArithmeticException(
                    "ERR increment or decrement would overflow"
            );
        }
    }

    @Override
    public long lpush(String key, List<String> values) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.computeIfAbsent(
                key, k -> new ArrayDeque<>()
        );

        for (String value : values) {
            list.addFirst(value);
        }

        return list.size();
    }

    @Override
    public long rpush(String key, List<String> values) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.computeIfAbsent(
                key, k -> new ArrayDeque<>()
        );

        for (String value : values) {
            list.addLast(value);
        }

        return list.size();
    }

    @Override
    public String lpop(String key) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.get(key);

        if (list == null) {
            return null;
        }

        String value = list.pollFirst();

        if (list.isEmpty()) {
            lists.remove(key);
            expiry.remove(key);
        }

        return value;
    }

    @Override
    public String rpop(String key) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.get(key);

        if (list == null) {
            return null;
        }

        String value = list.pollLast();

        if (list.isEmpty()) {
            lists.remove(key);
            expiry.remove(key);
        }

        return value;
    }

    @Override
    public long llen(String key) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.get(key);
        return list == null ? 0 : list.size();
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.get(key);
        if (list == null) {
            return List.of();
        }

        int size = list.size();

        // Negative indexes count from the end.
        if (start < 0) start += size;
        if (stop < 0) stop += size;

        // Clamp indexes to valid boundaries.
        start = Math.max(0, start);
        stop = Math.min(size - 1L, stop);

        if (start >= size || start > stop) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        int currentIndex = 0;

        for (String value : list) {
            if (currentIndex >= start && currentIndex <= stop) {
                result.add(value);
            }

            if (currentIndex > stop) {
                break;
            }

            currentIndex++;
        }

        return result;
    }

    @Override
    public String lindex(String key, long index) {
        if (isExpired(key)) {
            removeExpiredKey(key);
        }

        if (data.containsKey(key)) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value"
            );
        }

        Deque<String> list = lists.get(key);
        if (list == null) {
            return null;
        }

        if (index < 0) {
            index += list.size();
        }

        if (index < 0 || index >= list.size()) {
            return null;
        }

        int currentIndex = 0;

        for (String value : list) {
            if (currentIndex == index) {
                return value;
            }
            currentIndex++;
        }

        return null;
    }

    private boolean isExpired(String key) {
        if (!data.containsKey(key) && !lists.containsKey(key)) {
            return false;
        }

        Long expiryTime = expiry.get(key);

        return expiryTime != null &&
                System.currentTimeMillis() >= expiryTime;
    }

    private void removeExpiredKey(String key) {
        data.remove(key);
        lists.remove(key);
        expiry.remove(key);
    }
}
