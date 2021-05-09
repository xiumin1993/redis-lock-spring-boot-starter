package com.xm.component.redislock.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetRedisLockException extends Exception {
    private final String key;

    @Override
    public String getMessage() {
        return "get redis lock fail，key = [" + key + "]";
    }
}
