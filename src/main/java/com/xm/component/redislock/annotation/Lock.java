package com.xm.component.redislock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被此注解标注的方法执行时会需要获取redis锁
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface Lock {
    String keyPrefix() default "";


    /**
     * 锁过期时间  单位：ms(毫秒)
     * 若expirationTime = 0 则锁没有过期时间，方法结束后会自动释放锁
     *
     * @return
     */
    long expirationTime() default 0;

    /**
     * 重试次数，
     * 0 不重试
     * < 0 无限次重试
     *
     * @return
     */
    int retry() default 0;

    /**
     * 获取锁重试间隔时间，单位：毫秒
     *
     * @return
     */
    long frequency() default 200;

    /**
     * 是否使用全局配置的参数
     *
     * @return
     */
    boolean useGlobalConfig() default true;
}
