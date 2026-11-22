package com.redis.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RespEncoder {
    public ByteBuffer encode(RespResponse response) {
        String encoded;

        switch(response.type()) {
            case SIMPLE_STRING -> encoded = "+" + response.value() + "\r\n";
            case ERROR -> encoded = "-" + response.value() + "\r\n";
            case INTEGER -> encoded = ":" + response.value() + "\r\n";
            case BULK_STRING -> {
                byte[] payload = response.value()
                        .getBytes(StandardCharsets.UTF_8);

                String header = "$" + payload.length + "\r\n";
                ByteBuffer buffer = ByteBuffer.allocate(
                        header.getBytes(StandardCharsets.UTF_8).length
                                + payload.length + 2
                );
                buffer.put(header.getBytes(StandardCharsets.UTF_8));
                buffer.put(payload);
                buffer.put((byte) '\r');
                buffer.put((byte) '\n');
                buffer.flip();

                return buffer;
            }
            case NULL_BULK_STRING -> encoded = "$-1\r\n";
            case ARRAY -> {
                return encodeArray(response.elements());
            }
            default -> throw new IllegalStateException(
                    "Unsupported response type"
            );
        }
        return ByteBuffer.wrap(
                encoded.getBytes(StandardCharsets.UTF_8)
        );
    }

    private ByteBuffer encodeArray(List<RespResponse> elements) {
        StringBuilder result = new StringBuilder();
        result.append("*").append(elements.size()).append("\r\n");

        for (RespResponse element : elements) {
            ByteBuffer encoded = encode(element);
            byte[] bytes = new byte[encoded.remaining()];
            encoded.get(bytes);
            result.append(new String(bytes, StandardCharsets.UTF_8));
        }

        return ByteBuffer.wrap(result.toString().getBytes(StandardCharsets.UTF_8));
    }
}
