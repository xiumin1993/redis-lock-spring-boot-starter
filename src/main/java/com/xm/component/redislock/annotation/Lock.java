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
     * @return 过期时间
     */
    long expirationTime() default 0;

    /**
     * @return 重试次数, 0重试, 小于0无限次重试
     */
    int retry() default 0;

    /**
     * @return 获取锁重试间隔时间
     */
    long frequency() default 200;

    /**
     * @return 是否使用全局配置的参数
     */
    boolean useGlobalConfig() default true;
}
