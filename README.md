# xm-component

#### 介绍

基于spring-boot-start-data-redis的分布式锁

#### redis lock 介绍

引入依赖

```
        <dependency>
            <groupId>xm.test</groupId>
            <artifactId>redis-lock-spring-boot-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

### 使用说明

#### 1. 注解形式

多个线程调用且name参数相同时，只有一个线程可获取到锁。其他线程则会抛出 GetRedisLockException

 ```
    @Lock(keyPrefix = "HELLO:")
    public String sayHello(@LockKey String name) throws InterruptedException{
        Thread.sleep(5000);
        return "hello " + name;
    }
```

@LockKey 注解会将被标注的参数当作锁的key后缀, 具体实现是 遍历所有被@LockKey标注的参数, 如果参数为null，跳过此参数; 如果参数实现了RedisLockKey接口，则调用getKey()方法，
否则调用参数的toString()方法，

所以上面例子的完整 key为: "HELLO:" + name.toString();

#### 2. 手动方式

可以将要执行的代码作为Lambda表达式使用，可以不用手动释放锁

```
    @Resource
    private RedisLockComponent redisLockComponent;
    
    public String sayHello(String name){
        try {
            return redisLockComponent.execOnLocked("HELLO:" + name, () -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "hello " + name;
            });
        } catch (GetRedisLockException e) {
            log.error("sayHello异常：", e);
            return "服务繁忙";
        }
    }
```

或者普通形式, 需要手动释放锁

```
    @Resource
    private RedisLockComponent redisLockComponent;

    public String sayHello(String name){
        RedisLock lock = null;
        try {
            redisLockComponent.tryLockElseException("HELLO:" + name);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello " + name;
        } catch (GetRedisLockException e) {
            log.error("sayHello异常：", e);
            return "服务繁忙";
        }finally {
            redisLockComponent.unlock(lock);
        }
        
    }
```
