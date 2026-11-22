package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class LIndexCommand implements Command {

    private final StorageEngine storage;

    public LIndexCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 2) {
            return RespResponse.error(
                    "ERR wrong number of arguments for 'lindex' command"
            );
        }

        try {
            long index = Long.parseLong(arguments.get(1));
            String value = storage.lindex(arguments.get(0), index);

            return value == null
                    ? RespResponse.nullBulkString()
                    : RespResponse.bulkString(value);

        } catch (NumberFormatException e) {
            return RespResponse.error("ERR value is not an integer or out of range");
        } catch (IllegalStateException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}