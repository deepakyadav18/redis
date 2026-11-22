package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;
import java.util.List;

public class LlenCommand implements Command {

    private final StorageEngine storage;

    public LlenCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 1) {
            return RespResponse.error(
                    "ERR wrong number of arguments for 'llen' command"
            );
        }

        try {
            return RespResponse.integer(storage.llen(arguments.getFirst()));
        } catch (IllegalStateException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}