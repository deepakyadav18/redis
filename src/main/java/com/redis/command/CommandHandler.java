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
    private final IncrementCommand incrCommand;
    private final IncrementCommand decrCommand;
    private final IncrByCommand incrByCommand;
    private final LPushCommand lpushCommand;
    private final RPushCommand rpushCommand;
    private final RPopCommand rPopCommand;
    private final LPopCommand lPopCommand;
    private final LlenCommand llenCommand;
    private final LRangeCommand lRangeCommand;
    private final LIndexCommand lIndexCommand;


    public CommandHandler(StorageEngine storage) {
        this.setCommand = new SetCommand(storage);
        this.getCommand = new GetCommand(storage);
        this.deleteCommand = new DeleteCommand(storage);
        this.expireCommand = new ExpireCommand(storage);
        this.ttlCommand = new TtlCommand(storage);
        this.incrCommand = new IncrementCommand(storage, 1, "INCR");
        this.decrCommand = new IncrementCommand(storage, -1, "DECR");
        this.incrByCommand = new IncrByCommand(storage);
        this.lpushCommand = new LPushCommand(storage);
        this.rpushCommand = new RPushCommand(storage);
        this.rPopCommand = new RPopCommand(storage);
        this.lPopCommand = new LPopCommand(storage);
        this.llenCommand = new LlenCommand(storage);
        this.lRangeCommand = new LRangeCommand(storage);
        this.lIndexCommand = new LIndexCommand(storage);
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
            case "INCR" -> incrCommand.execute(arguments);
            case "DECR" -> decrCommand.execute(arguments);
            case "INCRBY" -> incrByCommand.execute(arguments);
            case "LPUSH" -> lpushCommand.execute(arguments);
            case "RPUSH" -> rpushCommand.execute(arguments);
            case "LPOP" -> lPopCommand.execute(arguments);
            case "RPOP" -> rPopCommand.execute(arguments);
            case "LLEN" -> llenCommand.execute(arguments);
            case "LRANGE" -> lRangeCommand.execute(arguments);
            case "LINDEX" -> lIndexCommand.execute(arguments);
            default -> RespResponse.error("ERR unknown command '" + commandName + "'");
        };
    }
}
