package com.xm.component.redislock.aop;

import com.xm.component.redislock.annotation.Lock;
import com.xm.component.redislock.annotation.LockKey;
import com.xm.component.redislock.config.RedisLockGlobalConfig;
import com.xm.component.redislock.core.RedisLock;
import com.xm.component.redislock.core.RedisLockComponent;
import com.xm.component.redislock.core.RedisLockKey;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;

/**
 * redis分布式锁切入点
 */
@Aspect
@Component
@Slf4j
public class RedisLockPointCut {
    @Resource
    private RedisLockComponent redisLockComponent;

    @Resource
    private RedisLockGlobalConfig redisLockGlobalConfig;

    @Around("@annotation(com.xm.component.redislock.annotation.Lock)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Lock cut = method.getAnnotation(Lock.class);
        Object[] args = pjp.getArgs();
        Parameter[] parameters = method.getParameters();

        StringBuilder key = new StringBuilder(cut.keyPrefix());
        for (int i = 0; i < parameters.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            if (parameters[i].isAnnotationPresent(LockKey.class)) {
                if (arg instanceof RedisLockKey) {
                    key.append(((RedisLockKey) arg).getKey());
                } else {
                    key.append(arg);
                }
            }
        }

        if (StringUtils.isEmpty(key.toString().trim())) {
            throw new IllegalArgumentException("redis lock key is empty");
        }

        boolean flag = cut.useGlobalConfig();
        //锁过期时间
        long expirationTime = cut.expirationTime();
        Duration duration;
        if (flag) {
            duration = redisLockGlobalConfig.getDuration();
        } else {
            duration = expirationTime <= 0 ? Duration.ZERO : Duration.ofMillis(expirationTime);
        }
        int retry = flag ? redisLockGlobalConfig.getRetry() : cut.retry();
        long frequency = flag ? redisLockGlobalConfig.getFrequency() : cut.frequency();
        RedisLock lock = null;
        try {
            lock = redisLockComponent.tryLockElseException(key.toString(), duration, retry, frequency);
            return pjp.proceed(args);
        } finally {
            if (lock != null && lock.isSuccess()) {
                redisLockComponent.unlock(lock);
            }
        }
    }

}
