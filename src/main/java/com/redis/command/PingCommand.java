package com.redis.command;

import com.redis.protocol.RespResponse;

import java.util.List;

public class PingCommand implements Command {
    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.isEmpty()) {
            return RespResponse.simpleString("PONG");
        }

        if (arguments.size() == 1) {
            return RespResponse.bulkString(arguments.getFirst());
        }

        throw new IllegalArgumentException("ERR wrong number of arguments for PING");
    }
}
