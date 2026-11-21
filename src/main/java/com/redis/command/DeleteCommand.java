package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class DeleteCommand implements Command{

    private final StorageEngine storage;

    public DeleteCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.isEmpty()) {
            return RespResponse.error(
                    "ERR wrong number of arguments for DEL"
            );
        }

        int deletedCount = storage.delete(arguments);
        return RespResponse.integer(deletedCount);
    }
}
