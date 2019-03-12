package com.threathunter.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * created by www.threathunter.cn
 */
public class RedisSingleNodePool extends JedisPool implements RedisPool {

    public RedisSingleNodePool(String host, int port) {
        this(new GenericObjectPoolConfig(), host, port, null);
    }

    public RedisSingleNodePool(GenericObjectPoolConfig config, String host, int port, String password) {
        super(config, host, port, 2000, password);
    }

    @Override
    public Jedis getResource(String tag) {
        return this.getResource();
    }
}
