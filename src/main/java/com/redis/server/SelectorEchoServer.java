package com.redis.server;

import com.redis.command.CommandHandler;
import com.redis.protocol.*;
import com.redis.storage.InMemoryStorage;
import com.redis.storage.StorageEngine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SelectorEchoServer {

    private static final RespParser parser = new RespParser();
    private static final StorageEngine storage = new InMemoryStorage();
    private static final CommandHandler commandHandler = new CommandHandler(storage);
    private static final RespEncoder respEncoder = new RespEncoder();

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(6379));

        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port 6379");

        while(true) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if(!key.isValid()) continue;

                    if(key.isAcceptable()) {
                        acceptClient(server, selector);
                    }
                    if(key.isReadable()) {
                        System.out.println("READ event received");
                        readFromClient(key);
                    }
                    if(key.isValid() && key.isWritable()) {
                        System.out.println("WRITE event received");
                        writeToClient(key);
                    }
                } catch (IOException e) {
                    closeClient(key);
                }
            }
        }
    }

    private static void acceptClient(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel client = server.accept();

        if(client == null) return;

        client.configureBlocking(false);
        SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
        clientKey.attach(new ClientState());
        System.out.println("Client connected: " + client.getRemoteAddress());
    }

    private static void readFromClient(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientState state = (ClientState) key.attachment();

        int bytesRead = client.read(state.readBuffer);
        System.out.println("Bytes read: " + bytesRead);
        if(bytesRead == -1) {
            System.out.println("Client finished sending data");

            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);

            if (state.writeQueue.isEmpty()) {
                closeClient(key);
            }
            return;
        }

        if(bytesRead == 0) {
            return;
        }

        // Switch buffer from writing mode to reading mode
        state.readBuffer.flip();

        while(true) {
            ParseResult result = parser.parse(state.readBuffer);
            System.out.println("Parser status: " + result.status());

            if (result.status() == ParseStatus.COMPLETE) {
                System.out.println("Parsed command: " + result.command());
                RespResponse response = commandHandler.handle(result.command());
                System.out.println("Command response: " + response);
                ByteBuffer responseBuffer = respEncoder.encode(response);
                state.writeQueue.add(responseBuffer);
                key.interestOps(
                        key.interestOps() | SelectionKey.OP_WRITE
                );
            } else if (result.status() == ParseStatus.INCOMPLETE) {
                break;
            } else {
                System.out.println("Invalid RESP: " + result.error());
                closeClient(key);
                return;
            }
        }

        state.readBuffer.compact();

        if (!state.readBuffer.hasRemaining()) {
            state.readBuffer = BufferUtils.grow(state.readBuffer);
        }

        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    private static void writeToClient(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientState state = (ClientState) key.attachment();

        while (!state.writeQueue.isEmpty()) {

            ByteBuffer response = state.writeQueue.peek();

            int bytesWritten = client.write(response);
            System.out.println("Bytes written: " + bytesWritten);

            // Partial write: wait for another OP_WRITE notification
            if (response.hasRemaining()) {
                break;
            }

            // Entire response has been written
            state.writeQueue.poll();
        }

        // No pending data: stop listening for write readiness
        if (state.writeQueue.isEmpty()) {
            key.interestOps(
                    key.interestOps() & ~SelectionKey.OP_WRITE
            );
        }
    }

    private static void closeClient(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException ignored) {
        }
    }

}
