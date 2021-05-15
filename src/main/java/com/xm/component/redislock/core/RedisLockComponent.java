package com.xm.component.redislock.core;

import com.xm.component.redislock.config.RedisLockGlobalConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author xiumin
 */
@Slf4j
public class RedisLockComponent {

    /**
     * 释放锁lua脚本
     */
    private static final byte[] RELEASE_LOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end".getBytes();
    @Resource
    private RedisLockGlobalConfig redisLockGlobalConfig;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisLockValueGenerator redisLockValueGenerator;


    public <T> T execOnLocked(String key, Supplier<T> supplier) throws GetRedisLockException {
        return execOnLocked(key, redisLockGlobalConfig.getDuration(), supplier);
    }

    public <T> T execOnLocked(String key, Duration duration, Supplier<T> supplier) throws GetRedisLockException {
        RedisLock lock = null;
        try {
            lock = this.tryLockElseException(key, duration);
            return supplier.get();
        } finally {
            if (lock != null && lock.isSuccess()) {
                unlock(lock);
            }
        }
    }

    /**
     * 获取锁，未获取到抛出异常
     *
     * @param key 锁key
     * @return 返回锁对象
     * @throws GetRedisLockException 获取锁异常
     */
    public RedisLock tryLockElseException(String key) throws GetRedisLockException {
        return tryLockElseException(key, redisLockGlobalConfig.getDuration(), redisLockGlobalConfig.getRetry(), redisLockGlobalConfig.getFrequency());

    }

    /**
     * 获取锁，未获取到抛出异常
     *
     * @param key      锁key
     * @param duration 锁过期时间;小于0,表示不过期
     * @return 返回锁对象
     * @throws GetRedisLockException 获取锁异常
     */
    public RedisLock tryLockElseException(String key, Duration duration) throws GetRedisLockException {
        return tryLockElseException(key, duration, redisLockGlobalConfig.getRetry(), redisLockGlobalConfig.getFrequency());

    }


    /**
     * 获取锁，未获取到抛出异常
     *
     * @param key       锁key
     * @param duration  锁过期时间
     * @param retry     重试次数， 0 次表示不重试 相当于 tryLockOrException 方法 ， 0 表示一直重试
     * @param frequency 重试间隔, 单位： 毫秒 (ms)
     * @return 返回锁
     * @throws GetRedisLockException 获取锁异常
     */
    public RedisLock tryLockElseException(String key, Duration duration, int retry, long frequency) throws GetRedisLockException {
        RedisLock lock = tryLock(key, duration, retry, frequency);
        if (lock.isSuccess()) {
            return lock;
        }
        throw new GetRedisLockException(key);

    }

    public RedisLock tryLock(String key) {
        return tryLock(key, redisLockGlobalConfig.getDuration(), redisLockGlobalConfig.getRetry(), redisLockGlobalConfig.getFrequency());
    }

    public RedisLock tryLock(String key, Duration duration) {
        return tryLock(key, duration, redisLockGlobalConfig.getRetry(), redisLockGlobalConfig.getFrequency());
    }

    public RedisLock tryLock(String key, Duration duration, int retry, long frequency) {
        Expiration expiration;
        if (duration.equals(Duration.ZERO)) {
            expiration = Expiration.persistent();
        } else {
            expiration = Expiration.from(duration);
        }
        String value = redisLockValueGenerator.generateValue(key);
        for (long i = 0; retry < 0 || i <= retry; i++) {
            if (i > 0) {
                if (frequency > 0) {
                    try {
                        Thread.sleep(frequency);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("重试获取redis lock第{}次,key = [{}]", i, key);
                }
            }

            Boolean flag = stringRedisTemplate.execute(
                    redisConnection -> redisConnection.set(key.getBytes(), value.getBytes(), expiration, RedisStringCommands.SetOption.SET_IF_ABSENT),
                    true);
            if (flag != null && flag) {
                RedisLock lock = new RedisLock(key, value);
                if (log.isDebugEnabled()) {
                    log.debug("获取redis lock成功, {}", lock);
                }
                return lock;
            }
        }
        return new RedisLock(key, null);
    }

    /**
     * 释放锁
     *
     * @param lock 锁
     */
    public void unlock(RedisLock lock) {
        Long execute = stringRedisTemplate.execute((RedisCallback<Long>) redisConnection ->
                redisConnection.eval(RELEASE_LOCK_LUA_SCRIPT, ReturnType.fromJavaType(Long.class), 1, lock.getKey().getBytes(), lock.getValue().getBytes()));
        boolean flag = execute != null && execute == 1;
        if (!flag) {
            log.warn("释放redis lock:{}失败,redis返回值:{}", lock, execute);
        } else if (log.isDebugEnabled()) {
            log.debug("释放redis lock成功 {}", lock);
        }
    }
}
