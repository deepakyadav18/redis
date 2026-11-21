package com.redis.command;

import com.redis.protocol.RespResponse;

import java.util.List;
import java.util.Locale;

public class CommandHandler {
    private final PingCommand pingCommand = new PingCommand();

    public RespResponse handle(List<String> commandParts) {
        if (commandParts == null || commandParts.isEmpty()) {
            return RespResponse.error("ERR empty command");
        }

        String commandName = commandParts.getFirst().toUpperCase(Locale.ROOT);
        List<String> arguments = commandParts.subList(1, commandParts.size());

        return switch (commandName) {
            case "PING" -> pingCommand.execute(arguments);
            default -> RespResponse.error("ERR unknown command '" + commandName + "'");
        };
    }
}
