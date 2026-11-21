package com.redis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NonBlockingEchoServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(6379));
        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port 6379");


        while(true) {

            // wait for events
            selector.select();

            // get ready events
            var iterator = selector.selectedKeys().iterator();

            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if(key.isAcceptable()) {
                    // Handle new connection

                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);

                    clientChannel.register(selector, SelectionKey.OP_READ);

                    System.out.println("New client connected");

                }

                if(key.isReadable()) {
                    // Handle incoming data

                    SocketChannel client = (SocketChannel) key.channel();

                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int bytesRead = client.read(buffer);

                    if(bytesRead == -1) {
                        client.close();
                        key.cancel();
                        continue;
                    }

                    if(bytesRead > 0) {
                        buffer.flip();
                        client.write(buffer);
                    }

                }
            }
        }
    }
}
