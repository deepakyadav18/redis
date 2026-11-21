package com.redis.server;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClientState {
    ByteBuffer readBuffer = ByteBuffer.allocate(4096);
    final Queue<ByteBuffer> writeQueue = new ArrayDeque<>();
}
