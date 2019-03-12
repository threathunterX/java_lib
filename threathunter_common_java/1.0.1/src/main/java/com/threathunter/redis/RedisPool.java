package com.threathunter.redis;

import redis.clients.jedis.Jedis;

/**
 * created by www.threathunter.cn
 */
public interface RedisPool {

    Jedis getResource();

    Jedis getResource(String key);

    void returnResource(Jedis object);

    void returnBrokenResource(Jedis object);

    void destroy();
}
