package com.redis.server;

import java.nio.ByteBuffer;

public class BufferUtils {
    private BufferUtils() {
    }

    public static ByteBuffer grow(ByteBuffer oldBuffer) {
        int oldCapacity = oldBuffer.capacity();
        int newCapacity = oldCapacity * 2;
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        oldBuffer.flip();

        newBuffer.put(oldBuffer);
        return newBuffer;
    }
}
