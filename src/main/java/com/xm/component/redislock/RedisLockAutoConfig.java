package com.xm.component.redislock;

import com.xm.component.redislock.aop.RedisLockPointCut;
import com.xm.component.redislock.config.RedisLockGlobalConfig;
import com.xm.component.redislock.core.RedisLockComponent;
import com.xm.component.redislock.core.RedisLockValueGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({RedisLockGlobalConfig.class})
@Import(RedisLockPointCut.class)
public class RedisLockAutoConfig {

    @Bean
    @ConditionalOnMissingBean(value = {RedisLockComponent.class})
    RedisLockComponent redisLockComponent() {
        return new RedisLockComponent();
    }

    @Bean
    @ConditionalOnMissingBean(value = {RedisLockValueGenerator.class})
    RedisLockValueGenerator redisLockValueGenerator(RedisLockGlobalConfig redisLockGlobalConfig) throws IllegalAccessException, InstantiationException {
        return redisLockGlobalConfig.getLockValueGenerator().newInstance();
    }


}
