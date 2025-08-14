package com.xm.component.redislock.config;

import com.xm.component.redislock.core.RedisLockValueGenerator;
import com.xm.component.redislock.core.UUIDRedisLockValueGenerator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 获取锁时的默认参数
 */
@ConfigurationProperties(prefix = "redis-lock")
@Data
public class RedisLockGlobalConfig {
    /**
     * 默认是持久化锁
     */
    private Duration duration = Duration.ZERO;
    /**
     * 默认不重试获取锁
     */
    private int retry = 0;
    /**
     * 默认每次重试锁获取阻塞 200ms
     */
    private long frequency = 200;
    /**
     * 默认锁的value为uuid
     */
    private Class<? extends RedisLockValueGenerator> lockValueGenerator = UUIDRedisLockValueGenerator.class;
}
