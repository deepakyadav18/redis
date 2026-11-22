
package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;

public class IncrementCommand implements Command {

    private final StorageEngine storage;
    private final long delta;
    private final String commandName;

    public IncrementCommand(
            StorageEngine storage,
            long delta,
            String commandName
    ) {
        this.storage = storage;
        this.delta = delta;
        this.commandName = commandName;
    }

    @Override
    public RespResponse execute(List<String> arguments) {
        if (arguments.size() != 1) {
            return RespResponse.error(
                    "ERR wrong number of arguments for " + commandName
            );
        }

        try {
            long result = storage.incrementBy(arguments.getFirst(), delta);
            return RespResponse.integer(result);

        } catch (NumberFormatException | ArithmeticException | IllegalStateException e) {
            return RespResponse.error(e.getMessage());
        }
    }
}