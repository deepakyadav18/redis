package com.redis.command;

import com.redis.protocol.RespResponse;

import java.util.List;

public interface Command {
    RespResponse execute(List<String> arguments);
}
