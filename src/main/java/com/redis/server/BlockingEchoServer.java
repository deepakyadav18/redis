package com.redis.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingEchoServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6379);

        System.out.println("Server started at port 6379");

        while(true) {
            // Wait for a client connection
            Socket clientSocket = serverSocket.accept();

            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // read from client
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;

            while((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                writer.println(message);
            }

            clientSocket.close();
        }
    }
}
