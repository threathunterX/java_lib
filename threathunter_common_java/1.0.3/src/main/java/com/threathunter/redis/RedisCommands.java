package com.threathunter.redis;

import redis.clients.jedis.*;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by daisy on 17-9-19
 */
public interface RedisCommands {
    String set(String key, String value);

    String set(String key, String value, String nxxx, String expx, long time);

    String get(String key);

    Boolean exists(String key);

    Long del(String... key);

    Long del(String key);

    String type(String key);

    Long expire(String key, int seconds);

    Long expireAt(String key, long unixTime);

    Long ttl(String key);

    String getSet(String key, String value);

    List<String> mget(String... keys);

    Long setnx(String key, String value);

    String setex(String key, int seconds, String value);

    String mset(String... keysvalues);

    Long msetnx(String... keysvalues);

    Long decrBy(String key, long integer);

    Long decr(String key);

    Long incrBy(String key, long integer);

    Double incrByFloat(String key, double value);

    Long incr(String key);

    Long append(String key, String value);

    String substr(String key, int start, int end);

    Long hset(String key, String field, String value);

    String hget(String key, String field);

    Long hsetnx(String key, String field, String value);

    String hmset(String key, Map<String, String> hash);

    List<String> hmget(String key, String... fields);

    Long hincrBy(String key, String field, long value);

    Double hincrByFloat(String key, String field, double value);

    Boolean hexists(String key, String field);

    Long hdel(String key, String... fields);

    Long hlen(String key);

    Set<String> hkeys(String key);

    List<String> hvals(String key);

    Map<String, String> hgetAll(String key);

    Long rpush(String key, String... strings);

    Long lpush(String key, String... strings);

    Long llen(String key);

    List<String> lrange(String key, long start, long end);

    String ltrim(String key, long start, long end);

    String lindex(String keys, long index);

    String lset(String key, long index, String value);

    Long lrem(String key, long count, String value);

    String lpop(String key);

    List<String> blpop(int timeout, String key);

    String rpop(String key);

    Long sadd(String key, String... members);

    Set<String> smembers(String key);

    Long srem(String key, String... members);

    String spop(String key);

    Set<String> spop(String key, long count);

    Long scard(String key);

    Boolean sismember(String key, String member);

    String srandmember(String key);

    List<String> srandmember(String key, int count);

    Long zadd(String key, double score, String member);

    Long zadd(String key, double score, String member, ZAddParams params);

    Long zadd(String key, Map<String, Double> scoreMembers);

    Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    Set<String> zrange(String key, long start, long end);

    Long zrem(String key, String... members);

    Double zincrby(String key, double score, String member);

    Double zincrby(String key, double score, String member, ZIncrByParams params);

    Long zrank(String key, String member);

    Long zrevrank(String key, String member);

    Set<String> zrevrange(String key, long start, long end);

    Set<Tuple> zrangeWithScores(String key, long start, long end);

    Set<Tuple> zrevrangeWithScores(String key, long start, long end);

    Long zcard(String key);

    Double zscore(String key, String member);

    List<String> sort(String key, SortingParams sortingParameters);

    Long zcount(String key, double min, double max);

    Long zcount(String key, String min, String max);

    Set<String> zrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, String min, String max);

    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

    Set<String> zrevrangeByScore(String key, double max, double min);

    Set<String> zrevrangeByScore(String key, String max, String min);

    Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);

    Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

    Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);

    Long zremrangeByRank(String key, long start, long end);

    Long zremrangeByScore(String key, double start, double end);

    Long zremrangeByScore(String key, String start, String end);

    Long strlen(String key);

    Long lpushx(String key, String... string);

    Long persist(String key);

    Long rpushx(String key, String... string);

    Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value);

    Boolean setbit(String key, long offset, boolean value);

    Boolean setbit(String key, long offset, String value);

    Boolean getbit(String key, long offset);

    Long setrange(String key, long offset, String value);

    String getrange(String key, long startOffset, long endOffset);

    Long bitpos(String key, boolean value);

    Long bitpos(String key, boolean value, BitPosParams params);

    void subscribe(JedisPubSub jedisPubSub, String... channels);

    Long publish(String channel, String message);

    Long pexpire(String key, long milliseconds);

    Long pexpireAt(String key, long millisecondsTimestamp);

    Long pttl(String key);

    String psetex(String key, long milliseconds, String value);

    String set(String key, String value, String nxxx);

    String set(String key, String value, String nxxx, String expx, int time);

    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor);

    ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params);

    ScanResult<Tuple> zscan(String key, String cursor);

    ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);
}
