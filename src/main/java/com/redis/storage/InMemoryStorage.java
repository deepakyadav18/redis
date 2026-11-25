package com.redis.storage;

import java.util.*;

public class InMemoryStorage implements StorageEngine {

    private final RedisDatabase database = new RedisDatabase();

    private static final String WRONG_TYPE =
            "WRONGTYPE Operation against a key holding the wrong kind of value";

    @Override
    public void set(String key, String value) {
        RedisObject object = RedisObject.stringRaw(value);
        database.put(key, object);
    }

    @Override
    public String get(String key) {
        RedisObject object = database.get(key);

        if (object == null) {
            return null;
        }

        if (object.getType() != RedisType.STRING) {
            throw new IllegalStateException(WRONG_TYPE);
        }

        return (String) object.getValue();
    }

    @Override
    public int delete(List<String> keys) {
        int deletedCount = 0;

        for (String key : keys) {
            if (database.remove(key) != null) {
                deletedCount++;
            }
        }

        return deletedCount;
    }

    @Override
    public boolean expire(String key, long seconds) {
        return database.expire(key, seconds);
    }

    @Override
    public long ttl(String key) {
        return database.ttl(key);
    }

    @Override
    public int activeExpire(int sampleSize) {
        return database.activeExpire(sampleSize);
    }

    @Override
    public long incrementBy(String key, long delta)
            throws NumberFormatException {

        RedisObject object = database.get(key);

        if (object == null) {
            RedisObject newObject = RedisObject.stringRaw(String.valueOf(delta));
            database.putPreservingExpiry(key, newObject);
            return delta;
        }

        if (object.getType() != RedisType.STRING) {
            throw new IllegalStateException(WRONG_TYPE);
        }

        String currentValue = (String) object.getValue();

        try {
            long value = Long.parseLong(currentValue);
            long incrementedValue = Math.addExact(value, delta);

            RedisObject updatedObject = RedisObject.stringRaw(String.valueOf(incrementedValue));

            // INCR preserves the existing TTL.
            database.putPreservingExpiry(key, updatedObject);

            return incrementedValue;

        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    "ERR Value is not an integer"
            );

        } catch (ArithmeticException e) {
            throw new ArithmeticException(
                    "ERR increment or decrement would overflow"
            );
        }
    }

    private Deque<String> getList(RedisObject object) {
        return (Deque<String>) object.getValue();
    }

    private RedisObject getListObject(String key) {
        RedisObject object = database.get(key);

        if (object == null) {
            return null;
        }

        if (object.getType() != RedisType.LIST) {
            throw new IllegalStateException(WRONG_TYPE);
        }

        return object;
    }

    private RedisObject createListObject() {
        return RedisObject.listDeque(new ArrayDeque<>());
    }

    @Override
    public long lpush(String key, List<String> values) {
        RedisObject object = getListObject(key);

        if (object == null) {
            object = createListObject();
            database.putPreservingExpiry(key, object);
        }

        Deque<String> list = getList(object);

        for (String value : values) {
            list.addFirst(value);
        }

        return list.size();
    }

    @Override
    public long rpush(String key, List<String> values) {
        RedisObject object = getListObject(key);

        if (object == null) {
            object = createListObject();
            database.putPreservingExpiry(key, object);
        }

        Deque<String> list = getList(object);

        for (String value : values) {
            list.addLast(value);
        }

        return list.size();
    }

    @Override
    public String lpop(String key) {
        RedisObject object = getListObject(key);

        if (object == null) {
            return null;
        }

        Deque<String> list = getList(object);

        String value = list.pollFirst();

        if (list.isEmpty()) {
            database.remove(key);
        }

        return value;
    }

    @Override
    public String rpop(String key) {
        RedisObject object = getListObject(key);

        if (object == null) {
            return null;
        }

        Deque<String> list = getList(object);

        String value = list.pollLast();

        if (list.isEmpty()) {
            database.remove(key);
        }

        return value;
    }

    @Override
    public long llen(String key) {
        RedisObject object = getListObject(key);

        if (object == null) {
            return 0;
        }

        return getList(object).size();
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        RedisObject object = getListObject(key);

        if (object == null) {
            return List.of();
        }

        Deque<String> list = getList(object);

        int size = list.size();

        if (start < 0) {
            start += size;
        }

        if (stop < 0) {
            stop += size;
        }

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
        RedisObject object = getListObject(key);

        if (object == null) {
            return null;
        }

        Deque<String> list = getList(object);

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
}