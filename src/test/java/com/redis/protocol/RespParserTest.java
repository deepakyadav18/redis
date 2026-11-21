
package com.redis.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RespParserTest {

    public static void main(String[] args) {

        testCompleteCommand();

        testFragmentedCommand();

        testPipelinedCommands();
    }

    private static void testCompleteCommand() {

        System.out.println("\n--- Test 1: Complete Command ---");

        RespParser parser = new RespParser();

        String input = "*2\r\n$3\r\nGET\r\n$4\r\nname\r\n";

        ByteBuffer buffer = ByteBuffer.wrap(
                input.getBytes(StandardCharsets.UTF_8)
        );

        ParseResult result = parser.parse(buffer);

        System.out.println("Status: " + result.status());
        System.out.println("Command: " + result.command());

        assert result.status() == ParseStatus.COMPLETE;
        assert result.command().equals(
                java.util.List.of("GET", "name")
        );
    }

    private static void testFragmentedCommand() {

        System.out.println("\n--- Test 2: Fragmented Command ---");

        RespParser parser = new RespParser();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // First network read
        String firstPart = "*2\r\n$3\r\nGET\r\n$4\r\nna";

        buffer.put(firstPart.getBytes(StandardCharsets.UTF_8));

        buffer.flip();

        ParseResult firstResult = parser.parse(buffer);

        System.out.println("First status: " + firstResult.status());
        System.out.println("Position after parsing: " + buffer.position());

        assert firstResult.status() == ParseStatus.INCOMPLETE;
        assert buffer.position() == 0;

        // Preserve incomplete bytes and switch back to write mode.
        buffer.compact();

        // Second network read
        String secondPart = "me\r\n";

        buffer.put(secondPart.getBytes(StandardCharsets.UTF_8));

        buffer.flip();

        ParseResult secondResult = parser.parse(buffer);

        System.out.println("Second status: " + secondResult.status());
        System.out.println("Command: " + secondResult.command());

        assert secondResult.status() == ParseStatus.COMPLETE;
        assert secondResult.command().equals(
                java.util.List.of("GET", "name")
        );
    }

    private static void testPipelinedCommands() {

        System.out.println("\n--- Test 3: Pipelined Commands ---");

        RespParser parser = new RespParser();

        String input =
                "*2\r\n$3\r\nGET\r\n$4\r\nname\r\n" +
                        "*2\r\n$3\r\nGET\r\n$4\r\ncity\r\n";

        ByteBuffer buffer = ByteBuffer.wrap(
                input.getBytes(StandardCharsets.UTF_8)
        );

        // Parse first command
        ParseResult first = parser.parse(buffer);

        System.out.println("First command: " + first.command());

        // Parse second command from the same buffer
        ParseResult second = parser.parse(buffer);

        System.out.println("Second command: " + second.command());

        assert first.status() == ParseStatus.COMPLETE;
        assert second.status() == ParseStatus.COMPLETE;

        assert first.command().equals(
                java.util.List.of("GET", "name")
        );

        assert second.command().equals(
                java.util.List.of("GET", "city")
        );
    }
}
