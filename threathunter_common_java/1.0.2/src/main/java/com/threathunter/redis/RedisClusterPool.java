package com.threathunter.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClusterInfoCache;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.exceptions.JedisNoReachableClusterNodeException;
import redis.clients.util.JedisClusterCRC16;
import redis.clients.util.SafeEncoder;

import java.util.*;

/**
 * Created by daisy on 17-9-18
 */
public class RedisClusterPool implements RedisPool {
    public static final short HASHSLOTS = 16384;
    protected static final int DEFAULT_TIMEOUT = 2000;
    protected static final int DEFAULT_MAX_REDIRECTIONS = 5;

    protected final JedisClusterInfoCache cache;

    private List<JedisPool> shuffledPools;

    public RedisClusterPool(Set<HostAndPort> nodes) {
        this(nodes, null);
    }

    public RedisClusterPool(Set<HostAndPort> nodes, String password) {
        this(nodes, 2000, null);
    }

    public RedisClusterPool(Set<HostAndPort> nodes, int timeout, String password) {
        this(nodes, timeout, new GenericObjectPoolConfig(), password);
    }

    public RedisClusterPool(Set<HostAndPort> clusterNodes, int timeout, GenericObjectPoolConfig poolConfig, String password){
        this.cache = new JedisClusterInfoCache(poolConfig, timeout, timeout, password);
        this.initialSlotsCache(clusterNodes, poolConfig, password);
    }

    @Override
    public Jedis getResource() {
        for (JedisPool pool : shuffledPools) {
            Jedis jedis = null;
            try {
                jedis = pool.getResource();

                if (jedis == null) {
                    continue;
                }

                String result = jedis.ping();

                if (result.equalsIgnoreCase("pong")) return jedis;

                jedis.close();
            } catch (JedisException ex) {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }

        throw new JedisNoReachableClusterNodeException("No reachable node in cluster");
    }

    @Override
    public Jedis getResource(String key) {
        int slot = JedisClusterCRC16.getSlot(SafeEncoder.encode(key));
        JedisPool pool = this.cache.getSlotPool(slot);
        if (pool != null) {
            return pool.getResource();
        } else {
            this.renewSlotCache();
            pool = this.cache.getSlotPool(slot);
            return pool != null ? pool.getResource() : this.getResource();
        }
    }

    @Override
    public void returnResource(Jedis object) {
        object.close();
    }

    @Override
    public void returnBrokenResource(Jedis object) {
        object.close();
    }

    @Override
    public void destroy() {
        this.close();
    }

    private void initialSlotsCache(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig, String password) {
        Iterator<HostAndPort> iter = nodes.iterator();

        while (iter.hasNext()) {
            HostAndPort hostAndPort = iter.next();
            Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
            if (password != null) {
                jedis.auth(password);
            }

            try {
                this.cache.discoverClusterNodesAndSlots(jedis);
                this.shuffledPools = this.cache.getShuffledNodesPool();
                break;
            } catch (Exception e) {
                ;
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

    public void renewSlotCache() {
        this.cache.renewClusterSlots(null);
    }

    public void renewSlotCache(Jedis jedis) {
        this.cache.renewClusterSlots(jedis);
        this.shuffledPools = this.cache.getShuffledNodesPool();
    }

    public void close() {
        this.cache.reset();
    }
}
