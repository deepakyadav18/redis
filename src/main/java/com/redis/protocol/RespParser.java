
package com.redis.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    private static final byte ARRAY_PREFIX = '*';
    private static final byte BULK_STRING_PREFIX = '$';

    private static final byte CR = '\r';
    private static final byte LF = '\n';

    public ParseResult parse(ByteBuffer buffer) {

        int startPosition = buffer.position();

        try {
            List<String> command = parseCommand(buffer);
            return ParseResult.complete(command);

        } catch (IncompleteDataException e) {

            // Restore position so incomplete bytes aren't consumed.
            buffer.position(startPosition);
            return ParseResult.incomplete();

        } catch (RuntimeException e) {

            return ParseResult.error(e.getMessage());
        }
    }

    private List<String> parseCommand(ByteBuffer buffer) {

        byte prefix = getByte(buffer);

        if (prefix != ARRAY_PREFIX) {
            throw new IllegalArgumentException("Expected RESP array");
        }

        int numberOfElements = readInteger(buffer);

        if (numberOfElements < 1 || numberOfElements > 1024) {
            throw new IllegalArgumentException("Invalid array length");
        }

        List<String> command = new ArrayList<>();

        for (int i = 0; i < numberOfElements; i++) {
            command.add(readBulkString(buffer));
        }

        return command;
    }

    private String readBulkString(ByteBuffer buffer) {

        byte prefix = getByte(buffer);

        if (prefix != BULK_STRING_PREFIX) {
            throw new IllegalArgumentException("Expected bulk string");
        }

        int length = readInteger(buffer);

        if (length < 0 || length > 1024 * 1024) {
            throw new IllegalArgumentException("Invalid bulk string length");
        }

        if (buffer.remaining() < length + 2) {
            throw new IncompleteDataException();
        }

        byte[] payload = new byte[length];
        buffer.get(payload);

        expectCRLF(buffer);

        return new String(payload, StandardCharsets.UTF_8);
    }

    private int readInteger(ByteBuffer buffer) {

        StringBuilder value = new StringBuilder();

        while (true) {
            byte current = getByte(buffer);

            if (current == CR) {
                expectLF(buffer);
                break;
            }

            if (current < '0' || current > '9') {
                throw new IllegalArgumentException("Invalid integer");
            }

            value.append((char) current);

            if (value.length() > 10) {
                throw new IllegalArgumentException("Integer too long");
            }
        }

        return Integer.parseInt(value.toString());
    }

    private byte getByte(ByteBuffer buffer) {

        if (!buffer.hasRemaining()) {
            throw new IncompleteDataException();
        }

        return buffer.get();
    }

    private void expectCRLF(ByteBuffer buffer) {

        if (getByte(buffer) != CR || getByte(buffer) != LF) {
            throw new IllegalArgumentException("Expected CRLF");
        }
    }

    private void expectLF(ByteBuffer buffer) {

        if (getByte(buffer) != LF) {
            throw new IllegalArgumentException("Expected LF");
        }
    }

    private static class IncompleteDataException
            extends RuntimeException {
    }
}
