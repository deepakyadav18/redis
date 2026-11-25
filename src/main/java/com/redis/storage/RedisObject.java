package com.redis.storage;

import java.util.Deque;
import java.util.Objects;

public class RedisObject {

    private final RedisType type;
    private RedisEncoding encoding;
    private Object value;

    public RedisObject(
            RedisType type,
            RedisEncoding encoding,
            Object value
    ) {
        this.type = Objects.requireNonNull(type);
        this.encoding = Objects.requireNonNull(encoding);
        this.value = Objects.requireNonNull(value);

        validate();
    }

    public RedisType getType() {
        return type;
    }

    public RedisEncoding getEncoding() {
        return encoding;
    }

    public Object getValue() {
        return value;
    }

    public void setEncoding(RedisEncoding encoding) {
        this.encoding = encoding;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private void validate() {
        if (type == RedisType.STRING) {
            if (encoding != RedisEncoding.RAW ||
                    !(value instanceof String)) {
                throw new IllegalArgumentException(
                        "Invalid STRING object representation"
                );
            }
            return;
        }

        if (type == RedisType.LIST) {
            if (encoding != RedisEncoding.DEQUE ||
                    !(value instanceof Deque<?>)) {
                throw new IllegalArgumentException(
                        "Invalid LIST object representation"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "Unsupported Redis type: " + type
        );
    }

    // Factory for STRING objects
    public static RedisObject stringRaw(String value) {
        return new RedisObject(
                RedisType.STRING,
                RedisEncoding.RAW,
                value
        );
    }

    // Factory for LIST objects
    public static RedisObject listDeque(Deque<String> value) {
        return new RedisObject(
                RedisType.LIST,
                RedisEncoding.DEQUE,
                value
        );
    }
}