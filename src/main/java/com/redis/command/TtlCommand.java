package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class TtlCommand implements Command{

    private final StorageEngine storage;

    public TtlCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 1) {
            return RespResponse.error(
                    "ERR wrong number of arguments for TTL"
            );
        }

        return RespResponse.integer(
                storage.ttl(arguments.getFirst())
        );
    }
}
