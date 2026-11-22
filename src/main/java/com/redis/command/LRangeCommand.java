package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class LRangeCommand implements Command {

    private final StorageEngine storage;

    public LRangeCommand(StorageEngine storage) {
        this.storage = storage;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 3) {
            return RespResponse.error(
                    "ERR wrong number of arguments for 'lrange' command"
            );
        }

        try {
            long start = Long.parseLong(arguments.get(1));
            long stop = Long.parseLong(arguments.get(2));

            List<String> values = storage.lrange(
                    arguments.get(0), start, stop
            );

            List<RespResponse> responses = values.stream()
                    .map(RespResponse::bulkString)
                    .toList();

            return RespResponse.array(responses);

        } catch (NumberFormatException e) {
            return RespResponse.error("ERR value is not an integer or out of range");
        } catch (IllegalStateException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}