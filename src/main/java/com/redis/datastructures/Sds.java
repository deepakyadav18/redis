
package com.redis.datastructures;

import java.util.Arrays;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

public class Sds {

    private static final int MAX_PREALLOC = 1024 * 1024;

    public enum HeaderType {
        TYPE_5,
        TYPE_8,
        TYPE_16,
        TYPE_32,
        TYPE_64
    }

    private byte[] buffer;
    private int length;
    private int capacity;
    private HeaderType headerType;

    private Sds(byte[] data) {
        this.length = data.length;
        this.capacity = data.length;
        this.headerType = selectHeaderType(length);

        // TYPE_5 cannot represent an allocated buffer with free space.
        if (headerType == HeaderType.TYPE_5) {
            this.headerType = HeaderType.TYPE_8;
        }

        this.buffer = Arrays.copyOf(data, capacity + 1);
        this.buffer[length] = 0;
    }

    public static Sds fromBytes(byte[] data) {
        Objects.requireNonNull(data, "data cannot be null");
        return new Sds(data);
    }

    public static Sds fromString(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        return fromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    public int length() {
        return length;
    }

    public int capacity() {
        return capacity;
    }

    public int available() {
        if (headerType == HeaderType.TYPE_5) {
            return 0;
        }

        return capacity - length;
    }

    public HeaderType getHeaderType() {
        return headerType;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(buffer, length);
    }

    public String getString() {
        return new String(buffer, 0, length, StandardCharsets.UTF_8);
    }

    public void append(byte[] suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");

        if (suffix.length == 0) {
            return;
        }

        ensureCapacity(suffix.length);

        System.arraycopy(suffix, 0, buffer, length, suffix.length);

        length += suffix.length;
        buffer[length] = 0;
    }

    public void append(String suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");
        append(suffix.getBytes(StandardCharsets.UTF_8));
    }

    public void append(Sds suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");

        append(suffix.getBytes());
    }

    private void ensureCapacity(int additionalBytes) {
        if (additionalBytes < 0) {
            throw new IllegalArgumentException("Additional bytes cannot be negative");
        }

        int required = Math.addExact(length, additionalBytes);

        if (required <= capacity) {
            return;
        }

        int newCapacity;

        if (required < MAX_PREALLOC) {
            newCapacity = Math.multiplyExact(required, 2);
        } else {
            newCapacity = Math.addExact(required, MAX_PREALLOC);
        }

        if (newCapacity < required) {
            newCapacity = required;
        }

        HeaderType newHeaderType = selectHeaderType(newCapacity);

        if (newHeaderType == HeaderType.TYPE_5) {
            newHeaderType = HeaderType.TYPE_8;
        }

        buffer = Arrays.copyOf(buffer, newCapacity + 1);
        capacity = newCapacity;
        headerType = newHeaderType;
    }

    public void removeFreeSpace() {
        int newCapacity = length;

        HeaderType newHeaderType = selectHeaderType(newCapacity);

        if (newHeaderType == HeaderType.TYPE_5) {
            newHeaderType = HeaderType.TYPE_8;
        }

        buffer = Arrays.copyOf(buffer, newCapacity + 1);
        capacity = newCapacity;
        headerType = newHeaderType;

        buffer[length] = 0;
    }

    public void clear() {
        length = 0;
        buffer[0] = 0;
    }

    public static HeaderType selectHeaderType(int size) {
        if (size < 32) {
            return HeaderType.TYPE_5;
        }

        if (size < 256) {
            return HeaderType.TYPE_8;
        }

        if (size < 65_536) {
            return HeaderType.TYPE_16;
        }

        return HeaderType.TYPE_32;
    }

    public void setLength(int newLength) {
        if (newLength < 0 || newLength > length) {
            throw new IllegalArgumentException("Invalid SDS length");
        }

        length = newLength;
        buffer[length] = 0;
    }

    public void removeRange(int start, int end) {
        if (start < 0 || end < start || end >= length) {
            throw new IllegalArgumentException("Invalid range");
        }

        int removedLength = end - start + 1;
        int remainingLength = length - end - 1;

        System.arraycopy(buffer, end + 1, buffer, start, remainingLength);

        length -= removedLength;
        buffer[length] = 0;
    }

    public byte[] range(int start, int end) {
        int normalizedStart = start < 0 ? length + start : start;
        int normalizedEnd = end < 0 ? length + end : end;

        normalizedStart = Math.max(normalizedStart, 0);
        normalizedEnd = Math.min(normalizedEnd, length - 1);

        if (normalizedStart > normalizedEnd || normalizedStart >= length) {
            return new byte[0];
        }

        return Arrays.copyOfRange(buffer, normalizedStart, normalizedEnd + 1);
    }

    public Sds rangeSds(int start, int end) {
        return Sds.fromBytes(range(start, end));
    }

    public void trim(byte[] trimSet) {
        Objects.requireNonNull(trimSet, "trimSet cannot be null");

        if (length == 0 || trimSet.length == 0) {
            return;
        }

        boolean[] trimLookup = new boolean[256];

        for (byte value : trimSet) {
            trimLookup[value & 0xFF] = true;
        }

        int start = 0;
        int end = length - 1;

        while (start <= end && trimLookup[buffer[start] & 0xFF]) {
            start++;
        }

        while (end >= start && trimLookup[buffer[end] & 0xFF]) {
            end--;
        }

        int newLength = end - start + 1;

        if (newLength == length) {
            return;
        }

        if (newLength > 0) {
            System.arraycopy(buffer, start, buffer, 0, newLength);
        }

        length = newLength;
        buffer[length] = 0;
    }

    public void trim(String trimSet) {
        Objects.requireNonNull(trimSet, "trimSet cannot be null");
        trim(trimSet.getBytes(StandardCharsets.UTF_8));
    }

    public int compare(Sds other) {
        Objects.requireNonNull(other, "other cannot be null");

        int commonLength = Math.min(length, other.length);

        for (int i = 0; i < commonLength; i++) {
            int left = buffer[i] & 0xFF;
            int right = other.buffer[i] & 0xFF;

            if (left != right) {
                return Integer.compare(left, right);
            }
        }

        return Integer.compare(length, other.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sds other)) {
            return false;
        }

        if (length != other.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (buffer[i] != other.buffer[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }

    @Override
    public String toString() {
        return getString();
    }
}
