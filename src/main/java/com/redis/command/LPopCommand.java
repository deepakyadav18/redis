
package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class LPopCommand implements Command {

    private final StorageEngine storage;

    public LPopCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {

        if (arguments.size() != 1) {
            return RespResponse.error(
                    "ERR wrong number of arguments for 'lpop' command"
            );
        }

        try {
            String value = storage.lpop(arguments.getFirst());

            if (value == null) {
                return RespResponse.nullBulkString();
            }

            return RespResponse.bulkString(value);

        } catch (IllegalStateException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}
