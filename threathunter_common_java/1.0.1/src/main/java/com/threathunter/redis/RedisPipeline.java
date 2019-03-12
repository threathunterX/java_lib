package com.threathunter.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.sortedset.ZAddParams;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class RedisPipeline implements Closeable, PipelineCommands {
    private final Jedis client;
    private final Pipeline pipeline;
    private final boolean autoTag;
    private final String wrappedTag;

    public RedisPipeline(Jedis jedis, String wrappedTag, boolean autoTag) {
        this.client = jedis;
        this.pipeline = jedis.pipelined();
        this.wrappedTag = wrappedTag;
        this.autoTag = autoTag;
    }

    public void sync() {
        this.pipeline.sync();
    }

    public List<Object> syncAndReturnAll() {
        return this.pipeline.syncAndReturnAll();
    }

    @Override
    public void close() throws IOException {
        this.pipeline.close();
        this.client.close();
    }

    @Override
    public Response<String> set(String key, String value) {
        return this.pipeline.set(autoTag ? wrappedTag + key : key, value);
    }

    @Override
    public Response<String> hmset(String key, Map<String, String> hash) {
        return this.pipeline.hmset(autoTag ? wrappedTag + key : key, hash);
    }

    @Override
    public Response<Map<String, String>> hgetAll(String key) {
        return this.pipeline.hgetAll(autoTag ? wrappedTag + key : key);
    }

    @Override
    public Response<Long> expire(String key, int seconds) {
        return this.pipeline.expire(autoTag ? wrappedTag + key : key, seconds);
    }

    @Override
    public Response<Long> expireAt(String key, long unixTime) {
        return this.pipeline.expireAt(autoTag ? wrappedTag + key : key, unixTime);
    }

    @Override
    public Response<Long> zadd(String key, double score, String member) {
        return this.pipeline.zadd(autoTag ? wrappedTag + key : key, score, member);
    }

    @Override
    public Response<Long> zadd(String key, double score, String member, ZAddParams params) {
        return this.pipeline.zadd(autoTag ? wrappedTag + key : key, score, member, params);
    }

    @Override
    public Response<Long> zadd(String key, Map<String, Double> scoreMembers) {
        return this.pipeline.zadd(autoTag ? wrappedTag + key : key, scoreMembers);
    }

    @Override
    public Response<Long> zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return this.pipeline.zadd(autoTag ? wrappedTag + key : key, scoreMembers, params);
    }

    @Override
    public Response<Long> zremrangeByScore(String key, double start, double end) {
        return this.pipeline.zremrangeByScore(autoTag ? wrappedTag + key : key, start, end);
    }

    @Override
    public Response<Long> zremrangeByScore(String key, String start, String end) {
        return this.pipeline.zremrangeByScore(autoTag ? wrappedTag + key : key, start, end);
    }
}
