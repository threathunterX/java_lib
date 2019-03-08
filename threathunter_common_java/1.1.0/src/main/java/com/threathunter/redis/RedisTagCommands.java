package com.threathunter.redis;

import redis.clients.jedis.*;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by daisy on 17-9-14
 */
public interface RedisTagCommands {
    String set(String tag, String key, String value);

    String set(String tag, String key, String value, String nxxx, String expx, long time);

    String get(String tag, String key);

    Boolean exists(String tag, String key);

    Long del(String tag, String... key);

    Long del(String tag, String key);

    String type(String tag, String key);

    Long expire(String tag, String key, int seconds);

    Long expireAt(String tag, String key, long unixTime);

    Long ttl(String tag, String key);

    String getSet(String tag, String key, String value);

    List<String> mget(String tag, String... keys);

    Long setnx(String tag, String key, String value);

    String setex(String tag, String key, int seconds, String value);

    String mset(String tag, String... keysvalues);

    Long msetnx(String tag, String... keysvalues);

    Long decrBy(String tag, String key, long integer);

    Long decr(String tag, String key);

    Long incrBy(String tag, String key, long integer);

    Double incrByFloat(String tag, String key, double value);

    Long incr(String tag, String key);

    Long append(String tag, String key, String value);

    String substr(String tag, String key, int start, int end);

    Long hset(String tag, String key, String field, String value);

    String hget(String tag, String key, String field);

    Long hsetnx(String tag, String key, String field, String value);

    String hmset(String tag, String key, Map<String, String> hash);

    List<String> hmget(String tag, String key, String... fields);

    Long hincrBy(String tag, String key, String field, long value);

    Double hincrByFloat(String tag, String key, String field, double value);

    Boolean hexists(String tag, String key, String field);

    Long hdel(String tag, String key, String... fields);

    Long hlen(String tag, String key);

    Set<String> hkeys(String tag, String key);

    List<String> hvals(String tag, String key);

    Map<String, String> hgetAll(String tag, String key);

    Long rpush(String tag, String key, String string);

    Long lpush(String tag, String key, String string);

    Long llen(String tag, String key);

    List<String> lrange(String tag, String key, long start, long end);

    String ltrim(String tag, String key, long start, long end);

    String lindex(String tag, String keys, long index);

    String lset(String tag, String key, long index, String value);

    Long lrem(String tag, String key, long count, String value);

    String lpop(String tag, String key);

    String rpop(String tag, String key);

    Long sadd(String tag, String key, String... members);

    Set<String> smembers(String tag, String key);

    Long srem(String tag, String key, String... members);

    String spop(String tag, String key);

    Set<String> spop(String tag, String key, long count);

    Long scard(String tag, String key);

    Boolean sismember(String tag, String key, String member);

    String srandmember(String tag, String key);

    List<String> srandmember(String tag, String key, int count);

    Long zadd(String tag, String key, double score, String member);

    Long zadd(String tag, String key, double score, String member, ZAddParams params);

    Long zadd(String tag, String key, Map<String, Double> scoreMembers);

    Long zadd(String tag, String key, Map<String, Double> scoreMembers, ZAddParams params);

    Set<String> zrange(String tag, String key, long start, long end);

    Long zrem(String tag, String key, String... members);

    Double zincrby(String tag, String key, double score, String member);

    Double zincrby(String tag, String key, double score, String member, ZIncrByParams params);

    Long zrank(String tag, String key, String member);

    Long zrevrank(String tag, String key, String member);

    Set<String> zrevrange(String tag, String key, long start, long end);

    Set<Tuple> zrangeWithScores(String tag, String key, long start, long end);

    Set<Tuple> zrevrangeWithScores(String tag, String key, long start, long end);

    Long zcard(String tag, String key);

    Double zscore(String tag, String key, String member);

    List<String> sort(String tag, String key, SortingParams sortingParameters);

    Long zcount(String tag, String key, double min, double max);

    Long zcount(String tag, String key, String min, String max);

    Set<String> zrangeByScore(String tag, String key, double min, double max);

    Set<String> zrangeByScore(String tag, String key, String min, String max);

    Set<String> zrangeByScore(String tag, String key, double min, double max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String tag, String key, double min, double max);

    Set<Tuple> zrangeByScoreWithScores(String tag, String key, String min, String max);

    Set<Tuple> zrangeByScoreWithScores(String tag, String key, double min, double max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String tag, String key, String min, String max, int offset, int count);

    Set<String> zrevrangeByScore(String tag, String key, double max, double min);

    Set<String> zrevrangeByScore(String tag, String key, String max, String min);

    Set<String> zrevrangeByScore(String tag, String key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, double max, double min);

    Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, double max, double min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, String max, String min, int offset, int count);

    Set<String> zrevrangeByScore(String tag, String key, String max, String min, int offset, int count);

    Set<Tuple> zrevrangeByScoreWithScores(String tag, String key, String max, String min);

    Long zremrangeByRank(String tag, String key, long start, long end);

    Long zremrangeByScore(String tag, String key, double start, double end);

    Long zremrangeByScore(String tag, String key, String start, String end);

    Long strlen(String tag, String key);

    Long lpushx(String tag, String key, String... string);

    Long persist(String tag, String key);

    Long rpushx(String tag, String key, String... string);

    Long linsert(String tag, String key, BinaryClient.LIST_POSITION where, String pivot, String value);

    Boolean setbit(String tag, String key, long offset, boolean value);

    Boolean setbit(String tag, String key, long offset, String value);

    Boolean getbit(String tag, String key, long offset);

    Long setrange(String tag, String key, long offset, String value);

    String getrange(String tag, String key, long startOffset, long endOffset);

    Long bitpos(String tag, String key, boolean value);

    Long bitpos(String tag, String key, boolean value, BitPosParams params);

    Long pexpire(String tag, String key, long milliseconds);

    Long pexpireAt(String tag, String key, long millisecondsTimestamp);

    Long pttl(String tag, String key);

    String psetex(String tag, String key, long milliseconds, String value);

    String set(String tag, String key, String value, String nxxx);

    String set(String tag, String key, String value, String nxxx, String expx, int time);

    ScanResult<Map.Entry<String, String>> hscan(String tag, String key, String cursor);

    ScanResult<Map.Entry<String, String>> hscan(String tag, String key, String cursor, ScanParams params);

    ScanResult<Tuple> zscan(String tag, String key, String cursor);

    ScanResult<Tuple> zscan(String tag, String key, String cursor, ScanParams params);
}
