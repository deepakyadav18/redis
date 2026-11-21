package com.redis.command;

import com.redis.protocol.RespResponse;
import com.redis.storage.StorageEngine;

import java.util.List;
import java.util.Locale;

public class CommandHandler {
    private final PingCommand pingCommand = new PingCommand();
    private final SetCommand setCommand;
    private final GetCommand getCommand;
    private final DeleteCommand deleteCommand;
    private final ExpireCommand expireCommand;
    private final TtlCommand ttlCommand;

    public CommandHandler(StorageEngine storage) {
        this.setCommand = new SetCommand(storage);
        this.getCommand = new GetCommand(storage);
        this.deleteCommand = new DeleteCommand(storage);
        this.expireCommand = new ExpireCommand(storage);
        this.ttlCommand = new TtlCommand(storage);
    }

    public RespResponse handle(List<String> commandParts) {
        if (commandParts == null || commandParts.isEmpty()) {
            return RespResponse.error("ERR empty command");
        }

        String commandName = commandParts.getFirst().toUpperCase(Locale.ROOT);
        List<String> arguments = commandParts.subList(1, commandParts.size());

        return switch (commandName) {
            case "PING" -> pingCommand.execute(arguments);
            case "SET" -> setCommand.execute(arguments);
            case "GET" -> getCommand.execute(arguments);
            case "DEL" -> deleteCommand.execute(arguments);
            case "EXPIRE" -> expireCommand.execute(arguments);
            case "TTL" -> ttlCommand.execute(arguments);
            default -> RespResponse.error("ERR unknown command '" + commandName + "'");
        };
    }
}
