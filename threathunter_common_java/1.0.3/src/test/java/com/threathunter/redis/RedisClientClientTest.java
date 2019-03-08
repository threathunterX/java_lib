package com.threathunter.redis;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by daisy on 17-9-12
 */
public class RedisClientClientTest {

    @Test
    public void testRedisCluster() throws IOException {
        Set<HostAndPort> addresses = new HashSet<>();
        addresses.add(new HostAndPort("172.16.10.65", 6380));
        addresses.add(new HostAndPort("172.16.10.65", 6381));
        addresses.add(new HostAndPort("172.16.10.65", 6382));
        JedisCluster cluster = new JedisCluster(addresses);
        for (int i = 0; i < 10; i++) {
            cluster.set("{groupkey}key" + i, "value");
        }
        cluster.close();
    }

    @Test
    public void testRedisConnection() {
        Jedis jedis = new Jedis("172.16.10.65", 6380);
        jedis.set("keykey", "value");
    }
}
