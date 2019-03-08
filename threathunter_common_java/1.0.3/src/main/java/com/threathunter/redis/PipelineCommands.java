package com.threathunter.redis;

import redis.clients.jedis.Response;
import redis.clients.jedis.params.sortedset.ZAddParams;

import java.util.Map;

/**
 * Created by daisy on 17-9-21
 */
public interface PipelineCommands {

    Response<String> set(String key, String value);

    Response<String> hmset(String key, Map<String, String> hash);

    Response<Map<String, String>> hgetAll(String key);

    Response<Long> expire(String key, int seconds);

    Response<Long> expireAt(String key, long unixTime);

    Response<Long> zadd(String key, double score, String member);

    Response<Long> zadd(String key, double score, String member, ZAddParams params);

    Response<Long> zadd(String key, Map<String, Double> scoreMembers);

    Response<Long> zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    Response<Long> zremrangeByScore(String key, double start, double end);

    Response<Long> zremrangeByScore(String key, String start, String end);
}
