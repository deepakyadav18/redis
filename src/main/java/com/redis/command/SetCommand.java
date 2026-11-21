package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class SetCommand implements Command {

    private final StorageEngine storage;

    public SetCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 2) {
            return RespResponse.error(
                    "ERR wrong number of arguments for SET"
            );
        }

        String key = arguments.get(0);
        String value = arguments.get(1);

        storage.set(key, value);
        return RespResponse.simpleString("OK");
    }
}
