package com.threathunter.redis;

import org.junit.Test;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by daisy on 17-9-20
 */
public class RedisClientTest {

    @Test
    public void performanceJedisTest() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.flushAll();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            jedis.set("key" + i, "value" + i);
        }
        jedis.close();
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void performanceRedisClientTest() {
        RedisClient proxy = new RedisClient("127.0.0.1", 6379);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            proxy.set("key" + i, "value" + i);
        }
        proxy.close();
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void performanceJedisClusterTest() throws IOException {
        Set<HostAndPort> set = new HashSet<>();
        set.add(new HostAndPort("172.16.10.65", 6380));
        set.add(new HostAndPort("172.16.10.65", 6381));
        set.add(new HostAndPort("172.16.10.65", 6382));
        JedisCluster cluster = new JedisCluster(set);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            cluster.set("key" + i, "value" + i);
        }
        cluster.close();
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void performanceRedisClusterTest() {
        RedisClient proxy = getRedisCluster();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            proxy.set("key" + i, "value" + i);
        }
        proxy.close();
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void performanceClusterPipelineTest() {
        RedisClient proxy = getRedisCluster();
        RedisPipeline pipeline = proxy.getPipeline("{mytag}");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            pipeline.set("{mytag}"+ "key" + i, "value" + i);
        }
        pipeline.sync();
        proxy.close();
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void subscribeTest() throws InterruptedException {
        RedisClient proxy = getRedisCluster();
        String channel = "hellochannel";

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                proxy.publish(channel, "hello" + i);
            }
        });

        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
//                super.onMessage(channel, message);
                System.out.println(message);
            }
        };
        Thread subscribeThread = new Thread(() -> {
            proxy.subscribe(pubSub, channel);
        });

        thread.start();
        subscribeThread.start();

        System.out.println("waiting...");
        Thread.sleep(10000);

        pubSub.unsubscribe();
        System.out.println("stopped");
    }

    private RedisClient getRedisCluster() {
        String[] hostAndPorts = new String[3];
        hostAndPorts[0] = "172.16.10.65:6380";
        hostAndPorts[1] = "172.16.10.65:6381";
        hostAndPorts[2] = "172.16.10.65:6382";
        RedisClient proxy = new RedisClient(hostAndPorts);
        return proxy;
    }
}
