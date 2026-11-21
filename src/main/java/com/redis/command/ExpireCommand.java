package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class ExpireCommand implements Command{

    private final StorageEngine storage;

    public ExpireCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 2) {
            return RespResponse.error(
                    "ERR wrong number of arguments for EXPIRE"
            );
        }

        String key = arguments.getFirst();

        try {
            long seconds = Long.parseLong(arguments.get(1));
            boolean success = storage.expire(key, seconds);

            return RespResponse.integer(success ? 1 : 0);
        } catch (NumberFormatException e) {
            return RespResponse.error("ERR value is not an integer or out of range");
        }
    }
}
