package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class LPushCommand implements Command {

    private final StorageEngine storage;

    public LPushCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() < 2) {
            return RespResponse.error(
                    "ERR wrong number of arguments for LPUSH"
            );
        }

        String key = arguments.get(0);
        List<String> values = arguments.subList(1, arguments.size());

        try {
            long length = storage.lpush(key, values);
            return RespResponse.integer(length);
        } catch (IllegalStateException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}