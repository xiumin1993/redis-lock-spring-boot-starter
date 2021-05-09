package com.xm.component.redislock.core;

/**
 * 用于生成 lock value
 */
@FunctionalInterface
public interface RedisLockValueGenerator {
    String generateValue(String key);
}
