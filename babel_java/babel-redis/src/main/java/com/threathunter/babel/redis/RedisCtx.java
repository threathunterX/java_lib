package com.threathunter.babel.redis;

import com.threathunter.config.CommonDynamicConfig;
import com.threathunter.redis.RedisClient;
import redis.clients.jedis.JedisPool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.ResourceBundle;

/**
 * created by www.threathunter.cn
 */
public class RedisCtx {
    private static final int DEFAULT_TOTAL = 100;
    private static String host;
    private static int port;
    private static int total;
    private static String password;

    private static RedisClient redisClient;

    static {
        String[] redisCluster = null;
        if (CommonDynamicConfig.getInstance().getString("babel_server") != null) {
            redisCluster = CommonDynamicConfig.getInstance().getStringArray("redis_cluster");
            if (redisCluster == null || redisCluster.length <= 0) {
                host = CommonDynamicConfig.getInstance().getString("redis_host", "127.0.0.1");
                port = CommonDynamicConfig.getInstance().getInt("redis_port", 6379);
            }
            total = CommonDynamicConfig.getInstance().getInt("babel_max_total", DEFAULT_TOTAL);
            password = CommonDynamicConfig.getInstance().getString("redis_password", null);
        } else {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("babel");
            host = resourceBundle.getString("redis.host");
            port = Integer.parseInt(resourceBundle.getString("redis.port"));

            total = DEFAULT_TOTAL;
            if (resourceBundle.containsKey("redis.total")) {
                total = Integer.parseInt(resourceBundle.getString("redis.total"));
            }
            if (resourceBundle.containsKey("redis.password")) {
                password = resourceBundle.getString("redis.password");
            }
        }
        GenericObjectPoolConfig c = new GenericObjectPoolConfig();
        c.setMaxTotal(total);
        if (redisCluster != null && redisCluster.length > 0) {
            redisClient = new RedisClient(c, redisCluster, password);
        } else {
            redisClient = new RedisClient(c, host, port, password);
        }
    }

    public static void setHost(String redisHost) {
        host = redisHost;
    }

    public static void setPort(int redisPort) {
        port = redisPort;
    }

    public static RedisClient getRedisClient() {
        return redisClient;
    }
}
