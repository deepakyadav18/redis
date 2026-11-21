
package com.redis.protocol;

public record RespResponse(Type type, String value) {

    public enum Type {
        SIMPLE_STRING,
        BULK_STRING,
        ERROR,
        INTEGER
    }

    public static RespResponse simpleString(String value) {
        return new RespResponse(Type.SIMPLE_STRING, value);
    }

    public static RespResponse bulkString(String value) {
        return new RespResponse(Type.BULK_STRING, value);
    }

    public static RespResponse error(String value) {
        return new RespResponse(Type.ERROR, value);
    }

    public static RespResponse integer(long value) {
        return new RespResponse(Type.INTEGER, String.valueOf(value));
    }
}
