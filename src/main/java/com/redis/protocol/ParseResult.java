package com.redis.protocol;

import java.util.List;

public record ParseResult(
        ParseStatus status,
        List<String> command,
        String error
) {
    public static ParseResult complete(List<String> command) {
        return new ParseResult(ParseStatus.COMPLETE, command, null);
    }

    public static ParseResult incomplete() {
        return new ParseResult(ParseStatus.INCOMPLETE, null, null);
    }

    public static ParseResult error(String message) {
        return new ParseResult(ParseStatus.ERROR, null, message);
    }
}