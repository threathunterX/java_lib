package com.threathunter.redis;

import com.threathunter.config.CommonDynamicConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisMovedDataException;
import redis.clients.jedis.exceptions.JedisNoReachableClusterNodeException;
import redis.clients.jedis.exceptions.JedisRedirectionException;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.threathunter.redis.RedisKeyUtils.genTaggedKey;
import static com.threathunter.redis.RedisKeyUtils.genTaggedKeys;
import static com.threathunter.redis.RedisKeyUtils.genTaggedKeysValues;

/**
 * created by www.threathunter.cn
 */
public class RedisClient implements RedisTagCommands, RedisCommands {

    // TODO this should implement tagged command
    // TODO get resource from pool and do execute by jedis client
    // TODO remove RedisClient
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);
    private final RedisPool redisPool;

    private final boolean clusterMode;
    private final int retryCount;

    /**
     * Implicit, initial from {@link com.threathunter.config.CommonDynamicConfig}
     *
     * if redis_cluster is empty, initial with host and port, single node
     * else initial with cluster
     *
     * redis_cluster = 127.0.0.1:6379,127.0.0.1:6380
     *
     * redis_host = 127.0.0.1
     * redis_port = 6379
     */
    public RedisClient() {
        CommonDynamicConfig config = CommonDynamicConfig.getInstance();
        String[] addresses = config.getStringArray("redis_cluster");

        int maxClient = CommonDynamicConfig.getInstance().getInt("redis_max_client", 8);
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxClient);

        String password = CommonDynamicConfig.getInstance().getString("redis_password");
        if (addresses != null && addresses.length > 0) {
            this.redisPool = new RedisClusterPool(getHostAndPort(addresses), 2000, poolConfig, password);
            this.clusterMode = true;
        } else {
            this.redisPool = new RedisSingleNodePool(poolConfig,
                    CommonDynamicConfig.getInstance().getString("redis_host"),
                    CommonDynamicConfig.getInstance().getInt("redis_port"), password);
            this.clusterMode = false;
        }

        this.retryCount = CommonDynamicConfig.getInstance().getInt("redis_cluster_retry_count", 3);
    }

    /**
     * Explicit initial with specific host and port, single node
     * @param host
     * @param port
     */
    public RedisClient(String host, int port) {
        this(new GenericObjectPoolConfig(), host, port, null);
    }

    public RedisClient(String host, int port, String password) {
        this(new GenericObjectPoolConfig(), host, port, password);
    }

    public RedisClient(GenericObjectPoolConfig config, String host, int port, String password) {
        this.redisPool = new RedisSingleNodePool(config, host, port, password);
        this.clusterMode = false;
        this.retryCount = 3;
    }

    /**
     * Explicit initial with cluster addresses, cluster mode
     * @param addresses
     */
    public RedisClient(String[] addresses) {
        this(addresses, null);
    }

    public RedisClient(String[] addresses, String password) {
        this(new GenericObjectPoolConfig(), addresses, password);
    }

    public RedisClient(GenericObjectPoolConfig config, String[] addresses, String password) {
        this.redisPool = new RedisClusterPool(getHostAndPort(addresses), 2000, config, password);
        this.clusterMode = true;
        this.retryCount = 3;
    }

    public void close() {
        this.redisPool.destroy();
    }

    private Set<HostAndPort> getHostAndPort(String[] addresses) {
        Set<HostAndPort> set = new HashSet<>();
        for (String address : addresses) {
            String[] split = address.split(":");
            set.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
        }

        return set;
    }

    private <T> T execute(Function<Jedis, T> func, String key) {
        return execute(func, key, false);
    }

    private void action(Consumer<Jedis> consumer, String key) {
        action(consumer, key, false);
    }

    private void action(Consumer<Jedis> consumer, String key, boolean anyNode) {
        Jedis jedis = null;
        try {
            if (!clusterMode) {
                jedis = this.redisPool.getResource();
                consumer.accept(jedis);
            }

            if (key == null || key.isEmpty()) {
                if (!anyNode) {
                    throw new RuntimeException("[redis cluster] command key is null");
                }
            }

            for (int i = 0; i < this.retryCount; i++) {
                try {
                    if (anyNode) {
                        jedis = this.redisPool.getResource();
                    } else {
                        jedis = this.redisPool.getResource(key);
                    }

                    consumer.accept(jedis);
                } catch (JedisNoReachableClusterNodeException nrcne) {
                    throw nrcne;
                } catch (JedisConnectionException jce) {
                    if (jedis != null) {
                        jedis.close();
                        jedis = null;
                    }
                    if (i <= 1) {
                        ((RedisClusterPool) this.redisPool).renewSlotCache();
                        throw jce;
                    }
                } catch (JedisRedirectionException jre) {
                    if (jre instanceof JedisMovedDataException) {
                        ((RedisClusterPool) this.redisPool).renewSlotCache(jedis);
                        if (jedis != null) {
                            jedis.close();
                            jedis = null;
                        }
                    }
                }
            }
            throw new RuntimeException("[redis cluster] redis cluster is unreachable");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private <T> T execute(Function<Jedis, T> func, String key, boolean anyNode) {
        Jedis jedis = null;
        try {
            if (!clusterMode) {
                jedis = this.redisPool.getResource();
                return func.apply(jedis);
            }

            if (key == null || key.isEmpty()) {
                if (!anyNode) {
                    throw new RuntimeException("[redis cluster] command key is null");
                }
            }

            for (int i = 0; i < this.retryCount; i++) {
                try {
                    if (anyNode) {
                        jedis = this.redisPool.getResource();
                    } else {
                        jedis = this.redisPool.getResource(key);
                    }

                    return func.apply(jedis);
                } catch (JedisNoReachableClusterNodeException nrcne) {
                    throw nrcne;
                } catch (JedisConnectionException jce) {
                    if (jedis != null) {
                        jedis.close();
                        jedis = null;
                    }
                    if (i <= 1) {
                        ((RedisClusterPool) this.redisPool).renewSlotCache();
                        throw jce;
                    }
                } catch (JedisRedirectionException jre) {
                    if (jre instanceof JedisMovedDataException) {
                        ((RedisClusterPool) this.redisPool).renewSlotCache(jedis);
                        if (jedis != null) {
                            jedis.close();
                            jedis = null;
                        }
                    }
                }
            }
            throw new RuntimeException("[redis cluster] redis cluster is unreachable");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public RedisPipeline getPipeline() {
        return new RedisPipeline(this.redisPool.getResource(), null, false);
    }

    public RedisPipeline getPipeline(String key) {
        //LOGGER.info("ZJP.RedisClient.getPipeline.key: " + key);
        Boolean clusterMode = this.clusterMode;
        if (key.equals("metrics.nebula.online")){
            clusterMode = true;
        }
        //LOGGER.info("ZJP.RedisClient.clusterMode: {}", clusterMode);
        if (clusterMode) {
            String wrappedTag = String.format("{%s}", key);
            return new RedisPipeline(this.redisPool.getResource(wrappedTag), wrappedTag, true);
        }
        return new RedisPipeline(this.redisPool.getResource(), null, false);
    }

    @Override
    public String set(String key, String value) {
        return execute(jedis -> jedis.set(key, value), key);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return execute(jedis -> jedis.set(key, value, nxxx, expx, time), key);
    }

    @Override
    public String get(String key) {
        return execute(jedis -> jedis.get(key), key);
    }

    @Override
    public Boolean exists(String key) {
        return execute(jedis -> jedis.exists(key), key);
    }

    @Override
    public Long del(String... keys) {
        return execute(jedis -> jedis.del(keys), "");
    }

    @Override
    public Long del(String key) {
        return execute(jedis -> jedis.del(key), key);
    }

    @Override
    public String type(String key) {
        return execute(jedis -> jedis.type(key), key);
    }

    @Override
    public Long expire(String key, int seconds) {
        return execute(jedis -> jedis.expire(key, seconds), key);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return execute(jedis -> jedis.expireAt(key, unixTime), key);
    }

    @Override
    public Long ttl(String key) {
        return execute(jedis -> jedis.ttl(key), key);
    }

    @Override
    public String getSet(String key, String value) {
        return execute(jedis -> jedis.getSet(key, value), key);
    }

    @Override
    public List<String> mget(String... keys) {
        return execute(jedis -> jedis.mget(keys), "");
    }

    @Override
    public Long setnx(String key, String value) {
        return execute(jedis -> jedis.setnx(key, value), key);
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return execute(jedis -> jedis.setex(key, seconds, value), key);
    }

    @Override
    public String mset(String... keysvalues) {
        return execute(jedis -> jedis.mset(keysvalues), "");
    }

    @Override
    public Long msetnx(String... keysvalues) {
        return execute(jedis -> jedis.msetnx(keysvalues), "");
    }

    @Override
    public Long decrBy(String key, long integer) {
        return execute(jedis -> jedis.decrBy(key, integer), key);
    }

    @Override
    public Long decr(String key) {
        return execute(jedis -> jedis.decr(key), key);
    }

    @Override
    public Long incrBy(String key, long integer) {
        return execute(jedis -> jedis.incrBy(key, integer), key);
    }

    @Override
    public Double incrByFloat(String key, double value) {
        return execute(jedis -> jedis.incrByFloat(key, value), key);
    }

    @Override
    public Long incr(String key) {
        return execute(jedis -> jedis.incr(key), key);
    }

    @Override
    public Long append(String key, String value) {
        return execute(jedis -> jedis.append(key, value), key);
    }

    @Override
    public String substr(String key, int start, int end) {
        return execute(jedis -> jedis.substr(key, start, end), key);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return execute(jedis -> jedis.hset(key, field, value), key);
    }

    @Override
    public String hget(String key, String field) {
        return execute(jedis -> jedis.hget(key, field), key);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return execute(jedis -> jedis.hsetnx(key, field, value), key);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return execute(jedis -> jedis.hmset(key, hash), key);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return execute(jedis -> jedis.hmget(key, fields), key);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return execute(jedis -> jedis.hincrBy(key, field, value), key);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return execute(jedis -> jedis.hincrByFloat(key, field, value), key);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return execute(jedis -> jedis.hexists(key, field), key);
    }

    @Override
    public Long hdel(String key, String... fields) {
        return execute(jedis -> jedis.hdel(key, fields), key);
    }

    @Override
    public Long hlen(String key) {
        return execute(jedis -> jedis.hlen(key), key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return execute(jedis -> jedis.hkeys(key), key);
    }

    @Override
    public List<String> hvals(String key) {
        return execute(jedis -> jedis.hvals(key), key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return execute(jedis -> jedis.hgetAll(key), key);
    }

    @Override
    public Long rpush(String key, String... strings) {
        return execute(jedis -> jedis.rpush(key, strings), key);
    }

    @Override
    public Long lpush(String key, String... strings) {
        return execute(jedis -> jedis.lpush(key, strings), key);
    }

    @Override
    public Long llen(String key) {
        return execute(jedis -> jedis.llen(key), key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return execute(jedis -> jedis.lrange(key, start, end), key);
    }

    @Override
    public String ltrim(String key, long start, long end) {
        return execute(jedis -> jedis.ltrim(key, start, end), key);
    }

    @Override
    public String lindex(String key, long index) {
        return execute(jedis -> jedis.lindex(key, index), key);
    }

    @Override
    public String lset(String key, long index, String value) {
        return execute(jedis -> jedis.lset(key, index, value), key);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return execute(jedis -> jedis.lrem(key, count, value), key);
    }

    @Override
    public String lpop(String key) {
        return execute(jedis -> jedis.lpop(key), key);
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return execute(jedis -> jedis.blpop(1, key), key);
    }

    @Override
    public String rpop(String key) {
        return execute(jedis -> jedis.lpop(key), key);
    }

    @Override
    public Long sadd(String key, String... members) {
        return execute(jedis -> jedis.sadd(key, members), key);
    }

    @Override
    public Set<String> smembers(String key) {
        return execute(jedis -> jedis.smembers(key), key);
    }

    @Override
    public Long srem(String key, String... members) {
        return execute(jedis -> jedis.srem(key, members), key);
    }

    @Override
    public String spop(String key) {
        return execute(jedis -> jedis.spop(key), key);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return execute(jedis -> jedis.spop(key, count), key);
    }

    @Override
    public Long scard(String key) {
        return execute(jedis -> jedis.scard(key), key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return execute(jedis -> jedis.sismember(key, member), key);
    }

    @Override
    public String srandmember(String key) {
        return execute(jedis -> jedis.srandmember(key), key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return execute(jedis -> jedis.srandmember(key, count), key);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return execute(jedis -> jedis.zadd(key, score, member), key);
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        return execute(jedis -> jedis.zadd(key, score, member, params), key);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return execute(jedis -> jedis.zadd(key, scoreMembers), key);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return execute(jedis -> jedis.zadd(key, scoreMembers, params), key);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        return execute(jedis -> jedis.zrange(key, start, end), key);
    }

    @Override
    public Long zrem(String key, String... members) {
        return execute(jedis -> jedis.zrem(key, members), key);
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        return execute(jedis -> jedis.zincrby(key, score, member), key);
    }

    @Override
    public Double zincrby(String key, double score, String member, ZIncrByParams params) {
        return execute(jedis -> jedis.zincrby(key, score, member, params), key);
    }

    @Override
    public Long zrank(String key, String member) {
        return execute(jedis -> jedis.zrank(key, member), key);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return execute(jedis -> jedis.zrevrank(key, member), key);
    }

    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        return execute(jedis -> jedis.zrevrange(key, start, end), key);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        return execute(jedis -> jedis.zrangeWithScores(key, start, end), key);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        return execute(jedis -> jedis.zrevrangeWithScores(key, start, end), key);
    }

    @Override
    public Long zcard(String key) {
        return execute(jedis -> jedis.zcard(key), key);
    }

    @Override
    public Double zscore(String key, String member) {
        return execute(jedis -> jedis.zscore(key, member), key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return execute(jedis -> jedis.sort(key, sortingParameters), key);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return execute(jedis -> jedis.zcount(key, min, max), key);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return execute(jedis -> jedis.zcount(key, min, max), key);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return execute(jedis -> jedis.zrangeByScore(key, min, max), key);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return execute(jedis -> jedis.zrangeByScore(key, min, max), key);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return execute(jedis -> jedis.zrangeByScore(key, min, max, offset, count), key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return execute(jedis -> jedis.zrangeByScoreWithScores(key, min, max), key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return execute(jedis -> jedis.zrangeByScoreWithScores(key, min, max), key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return execute(jedis -> jedis.zrangeByScoreWithScores(key, min, max, offset, count), key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return execute(jedis -> jedis.zrangeByScoreWithScores(key, min, max, offset, count), key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return execute(jedis -> jedis.zrevrangeByScore(key, max, min), key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return execute(jedis -> jedis.zrevrangeByScore(key, max, min), key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return execute(jedis -> jedis.zrevrangeByScore(key, max, min, offset, count), key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min), key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count), key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count), key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return execute(jedis -> jedis.zrevrangeByScore(key, max, min, offset, count), key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min), key);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        return execute(jedis -> jedis.zremrangeByRank(key, start, end), key);
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        return execute(jedis -> jedis.zremrangeByScore(key, start, end), key);
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        return execute(jedis -> jedis.zremrangeByScore(key, start, end), key);
    }

    @Override
    public Long strlen(String key) {
        return execute(jedis -> jedis.strlen(key), key);
    }

    @Override
    public Long lpushx(String key, String... string) {
        return execute(jedis -> jedis.lpushx(key, string), key);
    }

    @Override
    public Long persist(String key) {
        return execute(jedis -> jedis.persist(key), key);
    }

    @Override
    public Long rpushx(String key, String... string) {
        return execute(jedis -> jedis.rpushx(key, string), key);
    }

    @Override
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return execute(jedis -> jedis.linsert(key, where, pivot, value), key);
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return execute(jedis -> jedis.setbit(key, offset, value), key);
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return execute(jedis -> jedis.setbit(key, offset, value), key);
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return execute(jedis -> jedis.getbit(key, offset), key);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return execute(jedis -> jedis.setrange(key, offset, value), key);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return execute(jedis -> jedis.getrange(key, startOffset, endOffset), key);
    }

    @Override
    public Long bitpos(String key, boolean value) {
        return execute(jedis -> jedis.bitpos(key, value), key);
    }

    @Override
    public Long bitpos(String key, boolean value, BitPosParams params) {
        return execute(jedis -> jedis.bitpos(key, value, params), key);
    }

    @Override
    public Long publish(String channel, String message) {
        return execute(jedis -> jedis.publish(channel, message), "", true);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return execute(jedis -> jedis.pexpire(key, milliseconds), key);
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        action(jedis -> jedis.subscribe(jedisPubSub, channels), "", true);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return execute(jedis -> jedis.pexpireAt(key, millisecondsTimestamp), key);
    }

    @Override
    public Long pttl(String key) {
        return execute(jedis -> jedis.pttl(key), key);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return execute(jedis -> jedis.psetex(key, milliseconds, value), key);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, int time) {
        return execute(jedis -> jedis.set(key, value, nxxx, expx, time), key);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return execute(jedis -> jedis.hscan(key, cursor), key);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return execute(jedis -> jedis.hscan(key, cursor, params), key);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return execute(jedis -> jedis.zscan(key, cursor), key);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return execute(jedis -> jedis.zscan(key, cursor, params), key);
    }

    @Override
    public String set(String tag, String key, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.set(taggedKey, value), taggedKey);
    }

    @Override
    public String set(String tag, String key, String value, String nxxx, String expx, long time) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.set(taggedKey, value, nxxx, expx, time), taggedKey);
    }

    @Override
    public String get(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.get(taggedKey), taggedKey);
    }

    @Override
    public Boolean exists(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.exists(taggedKey), taggedKey);
    }

    @Override
    public Long del(String tag, String... keys) {
        String[] taggedKeys = genTaggedKeys(tag, keys);
        return execute(jedis -> jedis.del(taggedKeys), taggedKeys[0]);
    }

    @Override
    public Long del(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.del(taggedKey), taggedKey);
    }

    @Override
    public String type(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.type(taggedKey), taggedKey);
    }

    @Override
    public Long expire(String tag, String key, int seconds) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.expire(taggedKey, seconds), taggedKey);
    }

    @Override
    public Long expireAt(String tag, String key, long unixTime) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.expireAt(taggedKey, unixTime), taggedKey);
    }

    @Override
    public Long ttl(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.ttl(taggedKey), taggedKey);
    }

    @Override
    public String getSet(String tag, String key, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.getSet(taggedKey, value), taggedKey);
    }

    @Override
    public List<String> mget(String tag, String... keys) {
        String[] taggedKeys = genTaggedKeys(tag, keys);
        return execute(jedis -> jedis.mget(taggedKeys), taggedKeys[0]);
    }

    @Override
    public Long setnx(String tag, String key, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.setnx(taggedKey, value), taggedKey);
    }

    @Override
    public String setex(String tag, String key, int seconds, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.setex(taggedKey, seconds, value), taggedKey);
    }

    @Override
    public String mset(String tag, String... keysvalues) {
        String[] taggedKeysValues = genTaggedKeysValues(tag, keysvalues);
        return execute(jedis -> jedis.mset(taggedKeysValues), taggedKeysValues[0]);
    }

    @Override
    public Long msetnx(String tag, String... keysvalues) {
        String[] taggedKeysValues = genTaggedKeysValues(tag, keysvalues);
        return execute(jedis -> jedis.msetnx(taggedKeysValues), taggedKeysValues[0]);
    }

    @Override
    public Long decrBy(String tag, String key, long integer) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.decrBy(taggedKey, integer), taggedKey);
    }

    @Override
    public Long decr(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.decr(taggedKey), taggedKey);
    }

    @Override
    public Long incrBy(String tag, String key, long integer) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.incrBy(taggedKey, integer), taggedKey);
    }

    @Override
    public Double incrByFloat(String tag, String key, double value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.incrByFloat(taggedKey, value), taggedKey);
    }

    @Override
    public Long incr(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.incr(taggedKey), taggedKey);
    }

    @Override
    public Long append(String tag, String key, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.append(taggedKey, value), taggedKey);
    }

    @Override
    public String substr(String tag, String key, int start, int end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.substr(taggedKey, start, end), taggedKey);
    }

    @Override
    public Long hset(String tag, String key, String field, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hset(taggedKey, field, value), taggedKey);
    }

    @Override
    public String hget(String tag, String key, String field) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hget(taggedKey, field), taggedKey);
    }

    @Override
    public Long hsetnx(String tag, String key, String field, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hsetnx(taggedKey, field, value), taggedKey);
    }

    @Override
    public String hmset(String tag, String key, Map<String, String> hash) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hmset(taggedKey, hash), taggedKey);
    }

    @Override
    public List<String> hmget(String tag, String key, String... fields) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hmget(taggedKey, fields), taggedKey);
    }

    @Override
    public Long hincrBy(String tag, String key, String field, long value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hincrBy(taggedKey, field, value), taggedKey);
    }

    @Override
    public Double hincrByFloat(String tag, String key, String field, double value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hincrByFloat(taggedKey, field, value), taggedKey);
    }

    @Override
    public Boolean hexists(String tag, String key, String field) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hexists(taggedKey, field), taggedKey);
    }

    @Override
    public Long hdel(String tag, String key, String... fields) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hdel(taggedKey, fields), taggedKey);
    }

    @Override
    public Long hlen(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hlen(taggedKey), taggedKey);
    }

    @Override
    public Set<String> hkeys(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hkeys(taggedKey), taggedKey);
    }

    @Override
    public List<String> hvals(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hvals(taggedKey), taggedKey);
    }

    @Override
    public Map<String, String> hgetAll(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hgetAll(taggedKey), key);
    }

    @Override
    public Long rpush(String tag, String key, String string) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.rpush(taggedKey, string), taggedKey);
    }

    @Override
    public Long lpush(String tag, String key, String string) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lpush(taggedKey, string), taggedKey);
    }

    @Override
    public Long llen(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.llen(taggedKey), taggedKey);
    }

    @Override
    public List<String> lrange(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lrange(taggedKey, start, end), taggedKey);
    }

    @Override
    public String ltrim(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.ltrim(taggedKey, start, end), taggedKey);
    }

    @Override
    public String lindex(String tag, String key, long index) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lindex(taggedKey, index), taggedKey);
    }

    @Override
    public String lset(String tag, String key, long index, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lset(taggedKey, index, value), taggedKey);
    }

    @Override
    public Long lrem(String tag, String key, long count, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lrem(taggedKey, count, value), taggedKey);
    }

    @Override
    public String lpop(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lpop(taggedKey), taggedKey);
    }

    @Override
    public String rpop(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.rpop(taggedKey), taggedKey);
    }

    @Override
    public Long sadd(String tag, String key, String... members) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.sadd(taggedKey, members), taggedKey);
    }

    @Override
    public Set<String> smembers(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.smembers(taggedKey), taggedKey);
    }

    @Override
    public Long srem(String tag, String key, String... members) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.srem(taggedKey, members), taggedKey);
    }

    @Override
    public String spop(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.spop(taggedKey), taggedKey);
    }

    @Override
    public Set<String> spop(String tag, String key, long count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.spop(taggedKey, count), taggedKey);
    }

    @Override
    public Long scard(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.scard(taggedKey), taggedKey);
    }

    @Override
    public Boolean sismember(String tag, String key, String member) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.sismember(taggedKey, member), taggedKey);
    }

    @Override
    public String srandmember(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.srandmember(taggedKey), taggedKey);
    }

    @Override
    public List<String> srandmember(String tag, String key, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.srandmember(taggedKey, count), taggedKey);
    }

    @Override
    public Long zadd(String tag, String key, double score, String member) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zadd(taggedKey, score, member), taggedKey);
    }

    @Override
    public Long zadd(String tag, String key, double score, String member, ZAddParams params) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zadd(taggedKey, score, member, params), taggedKey);
    }

    @Override
    public Long zadd(String tag, String key, Map<String, Double> scoreMembers) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zadd(taggedKey, scoreMembers), taggedKey);
    }

    @Override
    public Long zadd(String tag, String key, Map<String, Double> scoreMembers, ZAddParams params) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zadd(taggedKey, scoreMembers, params), taggedKey);
    }

    @Override
    public Set<String> zrange(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrange(taggedKey, start, end), taggedKey);
    }

    @Override
    public Long zrem(String tag, String key, String... members) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrem(taggedKey, members), taggedKey);
    }

    @Override
    public Double zincrby(String tag, String key, double score, String member) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zincrby(taggedKey, score, member), taggedKey);
    }

    @Override
    public Double zincrby(String tag, String key, double score, String member, ZIncrByParams params) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zincrby(taggedKey, score, member, params), taggedKey);
    }

    @Override
    public Long zrank(String tag, String key, String member) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrank(taggedKey, member), taggedKey);
    }

    @Override
    public Long zrevrank(String tag, String key, String member) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrank(taggedKey, member), taggedKey);
    }

    @Override
    public Set<String> zrevrange(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrange(taggedKey, start, end), taggedKey);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeWithScores(taggedKey, start, end), taggedKey);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeWithScores(taggedKey, start, end), taggedKey);
    }

    @Override
    public Long zcard(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zcard(taggedKey), taggedKey);
    }

    @Override
    public Double zscore(String tag, String key, String member) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zscore(taggedKey, member), taggedKey);
    }

    @Override
    public List<String> sort(String tag, String key, SortingParams sortingParameters) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.sort(taggedKey, sortingParameters), taggedKey);
    }

    @Override
    public Long zcount(String tag, String key, double min, double max) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zcount(taggedKey, min, max), taggedKey);
    }

    @Override
    public Long zcount(String tag, String key, String min, String max) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zcount(taggedKey, min, max), taggedKey);
    }

    @Override
    public Set<String> zrangeByScore(String tag, String key, double min, double max) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScore(taggedKey, min, max), taggedKey);
    }

    @Override
    public Set<String> zrangeByScore(String tag, String key, String min, String max) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScore(taggedKey, min, max), taggedKey);
    }

    @Override
    public Set<String> zrangeByScore(String tag, String key, double min, double max, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScore(taggedKey, min, max, offset, count), taggedKey);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String tag, String key, double min, double max) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScoreWithScores(taggedKey, min, max), taggedKey);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String tag, String key, String min, String max) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScoreWithScores(taggedKey, min, max), taggedKey);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String tag, String key, double min, double max, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScoreWithScores(taggedKey, min, max, offset, count), taggedKey);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String tag, String key, String min, String max, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrangeByScoreWithScores(taggedKey, min, max, offset, count), taggedKey);
    }

    @Override
    public Set<String> zrevrangeByScore(String tag, String key, double max, double min) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScore(taggedKey, max, min), taggedKey);
    }

    @Override
    public Set<String> zrevrangeByScore(String tag, String key, String max, String min) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScore(taggedKey, max, min), taggedKey);
    }

    @Override
    public Set<String> zrevrangeByScore(String tag, String key, double max, double min, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScore(taggedKey, max, min, offset, count), taggedKey);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, double max, double min) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(taggedKey, max, min), taggedKey);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, double max, double min, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(taggedKey, max, min, offset, count), taggedKey);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, String max, String min, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(taggedKey, max, min, offset, count), taggedKey);
    }

    @Override
    public Set<String> zrevrangeByScore(String tag, String key, String max, String min, int offset, int count) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScore(taggedKey, max, min, offset, count), taggedKey);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, String max, String min) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zrevrangeByScoreWithScores(taggedKey, max, min), taggedKey);
    }

    @Override
    public Long zremrangeByRank(String tag, String key, long start, long end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zremrangeByRank(taggedKey, start, end), taggedKey);
    }

    @Override
    public Long zremrangeByScore(String tag, String key, double start, double end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zremrangeByScore(taggedKey, start, end), taggedKey);
    }

    @Override
    public Long zremrangeByScore(String tag, String key, String start, String end) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zremrangeByScore(taggedKey, start, end), taggedKey);
    }

    @Override
    public Long strlen(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.strlen(taggedKey), taggedKey);
    }

    @Override
    public Long lpushx(String tag, String key, String... string) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.lpushx(taggedKey, string), taggedKey);
    }

    @Override
    public Long persist(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.persist(taggedKey), taggedKey);
    }

    @Override
    public Long rpushx(String tag, String key, String... string) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.rpushx(taggedKey, string), taggedKey);
    }

    @Override
    public Long linsert(String tag, String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.linsert(taggedKey, where, pivot, value), taggedKey);
    }

    @Override
    public Boolean setbit(String tag, String key, long offset, boolean value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.setbit(taggedKey, offset, value), taggedKey);
    }

    @Override
    public Boolean setbit(String tag, String key, long offset, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.setbit(taggedKey, offset, value), taggedKey);
    }

    @Override
    public Boolean getbit(String tag, String key, long offset) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.getbit(taggedKey, offset), taggedKey);
    }

    @Override
    public Long setrange(String tag, String key, long offset, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.setrange(taggedKey, offset, value), taggedKey);
    }

    @Override
    public String getrange(String tag, String key, long startOffset, long endOffset) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.getrange(taggedKey, startOffset, endOffset), taggedKey);
    }

    @Override
    public Long bitpos(String tag, String key, boolean value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.bitpos(taggedKey, value), taggedKey);
    }

    @Override
    public Long bitpos(String tag, String key, boolean value, BitPosParams params) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.bitpos(taggedKey, value, params), taggedKey);
    }

    @Override
    public Long pexpire(String tag, String key, long milliseconds) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.pexpire(taggedKey, milliseconds), taggedKey);
    }

    @Override
    public Long pexpireAt(String tag, String key, long millisecondsTimestamp) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.pexpireAt(taggedKey, millisecondsTimestamp), taggedKey);
    }

    @Override
    public Long pttl(String tag, String key) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.pttl(taggedKey), taggedKey);
    }

    @Override
    public String psetex(String tag, String key, long milliseconds, String value) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.psetex(taggedKey, milliseconds, value), taggedKey);
    }

    @Override
    public String set(String tag, String key, String value, String nxxx) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.set(taggedKey, value, nxxx), taggedKey);
    }

    @Override
    public String set(String tag, String key, String value, String nxxx, String expx, int time) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.set(taggedKey, value, nxxx, expx, time), taggedKey);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String tag, String key, String cursor) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hscan(taggedKey, cursor), taggedKey);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String tag, String key, String cursor, ScanParams params) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.hscan(taggedKey, cursor, params), taggedKey);
    }

    @Override
    public ScanResult<Tuple> zscan(String tag, String key, String cursor) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zscan(taggedKey, cursor), taggedKey);
    }

    @Override
    public ScanResult<Tuple> zscan(String tag, String key, String cursor, ScanParams params) {
        String taggedKey = genTaggedKey(tag, key);
        return execute(jedis -> jedis.zscan(taggedKey, cursor, params), taggedKey);
    }
}
