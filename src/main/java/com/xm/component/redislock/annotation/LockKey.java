package com.xm.component.redislock.annotation;

import java.lang.annotation.*;

/**
 * 被此注解标识的参数会作为分布式锁的key后缀
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PARAMETER})
public @interface LockKey {
}
