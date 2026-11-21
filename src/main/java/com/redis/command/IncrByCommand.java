package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class IncrByCommand implements Command {

    private final StorageEngine storage;

    public IncrByCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 2) {
            return RespResponse.error(
                    "ERR wrong number of arguments for INCRBY"
            );
        }

        try {
            long delta = Long.parseLong(arguments.get(1));
            long result = storage.incrementBy(arguments.get(0), delta);
            return RespResponse.integer(result);
        } catch (NumberFormatException | ArithmeticException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}