package com.redis.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InMemoryStorage implements StorageEngine{

    private final HashMap<String, String> data = new HashMap<>();
    private final HashMap<String, Long> expiry = new HashMap<>();

    @Override
    public void set(String key, String value) {
        data.put(key, value);
        expiry.remove(key);
    }

    @Override
    public String get(String key) {
        if(isExpired(key)) {
            data.remove(key);
            expiry.remove(key);
            return null;
        }
        return data.get(key);
    }

    @Override
    public int delete(List<String> keys) {
        int deletedCount = 0;
        for(String key: keys) {
            if(data.remove(key) != null) {
                expiry.remove(key);
                deletedCount++;
            }
        }
        return deletedCount;
    }

    @Override
    public boolean expire(String key, long seconds) {
        if(!data.containsKey(key)) {
            return false;
        }

        if (seconds <= 0) {
            removeExpiredKey(key);
            return true;
        }

        long currTime = System.currentTimeMillis();
        expiry.put(key, currTime + seconds * 1000);
        return true;
    }

    @Override
    public long ttl(String key) {
        // Key doesn't exist
        if(!data.containsKey(key)) return -2;
        // Expiry doesn't exist
        if(!expiry.containsKey(key)) return -1;

        long expTime = expiry.get(key);
        long remainingTime = (expTime - System.currentTimeMillis())/1000;
        if(remainingTime <= 0) {
            removeExpiredKey(key);
            return -2;
        }
        return remainingTime;
    }

    @Override
    public int activeExpire(int sampleSize) {
        int expiredCount = 0;
        int sampledCount = 0;

        Iterator<Map.Entry<String, Long>> iterator = expiry.entrySet().iterator();

        long currTime = System.currentTimeMillis();

        while(iterator.hasNext() && sampledCount < sampleSize) {
            Map.Entry<String, Long> entry = iterator.next();
            sampledCount++;

            if(currTime >= entry.getValue()) {
                data.remove(entry.getKey());
                iterator.remove();
                expiredCount++;
            }
        }
        return expiredCount;
    }

    @Override
    public long incrementBy(String key, long delta) throws NumberFormatException {
        if(isExpired(key)) {
            removeExpiredKey(key);
        }

        String currentValue = data.get(key);

        if (currentValue == null) {
            data.put(key, String.valueOf(delta));
            return delta;
        }

        try {
            long val = Long.parseLong(data.get(key));
            long incVal = Math.addExact(val, delta);
            data.put(key, String.valueOf(incVal));
            return incVal;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("ERR Value is not an integer");
        } catch (ArithmeticException e) {
            throw new ArithmeticException("ERR increment or decrement would overflow");
        }
    }

    private boolean isExpired(String key) {
        Long expiryTime = expiry.get(key);

        return expiryTime != null &&
                System.currentTimeMillis() >= expiryTime;
    }

    private void removeExpiredKey(String key) {
        data.remove(key);
        expiry.remove(key);
    }
}
