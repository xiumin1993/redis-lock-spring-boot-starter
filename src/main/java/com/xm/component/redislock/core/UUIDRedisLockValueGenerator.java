package com.xm.component.redislock.core;

import java.util.UUID;

public class UUIDRedisLockValueGenerator implements RedisLockValueGenerator {
    @Override
    public String generateValue(String key) {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
