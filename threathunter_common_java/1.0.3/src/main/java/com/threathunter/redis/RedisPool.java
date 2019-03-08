package com.threathunter.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by daisy on 17-9-18
 */
public interface RedisPool {

    Jedis getResource();

    Jedis getResource(String key);

    void returnResource(Jedis object);

    void returnBrokenResource(Jedis object);

    void destroy();
}
