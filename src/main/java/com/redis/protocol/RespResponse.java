package com.redis.protocol;

import java.util.List;

public record RespResponse(Type type, String value, List<RespResponse> elements) {

    public enum Type {
        SIMPLE_STRING,
        BULK_STRING,
        ERROR,
        INTEGER,
        NULL_BULK_STRING,
        ARRAY
    }

    public static RespResponse simpleString(String value) {
        return new RespResponse(Type.SIMPLE_STRING, value, null);
    }

    public static RespResponse bulkString(String value) {
        return new RespResponse(Type.BULK_STRING, value, null);
    }

    public static RespResponse error(String value) {
        return new RespResponse(Type.ERROR, value, null);
    }

    public static RespResponse integer(long value) {
        return new RespResponse(Type.INTEGER, String.valueOf(value), null);
    }

    public static RespResponse nullBulkString() {
        return new RespResponse(Type.NULL_BULK_STRING, null, null);
    }

    public static RespResponse array(List<RespResponse> elements) {
        return new RespResponse(Type.ARRAY, null, List.copyOf(elements));
    }
}