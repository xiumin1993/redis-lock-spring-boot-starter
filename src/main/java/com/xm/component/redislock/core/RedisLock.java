package com.xm.component.redislock.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class RedisLock {
    @Getter
    private final String key;
    @Getter
    private final String value;

    public boolean isSuccess() {
        return value != null;
    }

    @Override
    public String toString() {
        return "[" + key + "] = [" + value + "]";
    }
}
