package com.threathunter.metrics;

import com.threathunter.metrics.model.LegendData;
import com.threathunter.redis.RedisClient;
import com.threathunter.redis.RedisPipeline;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by daisy on 2015/7/29.
 */
public class RedisMetricsImpl implements Metrics {
    private static final Logger logger = LoggerFactory.getLogger(RedisMetricsImpl.class);
    public final RedisClient redisClient;

    public RedisMetricsImpl(String host, int port) {
        redisClient = new RedisClient(host, port);
    }

    public RedisMetricsImpl(String host, int port, String password) {
        GenericObjectPoolConfig c1 = new GenericObjectPoolConfig();
        redisClient = new RedisClient(host, port, password);
    }

    public RedisMetricsImpl(String[] addresses) {
        redisClient = new RedisClient(addresses);
    }

    public RedisMetricsImpl(String[] addresses, String password) {
        redisClient = new RedisClient(addresses, password);
    }

    // weihong tag 这个地方是真正写redis的地方
    public void addMetrics(String db, String metricsName, Map<String, Object> tags, Double value, int expireSeconds) {
        String tag = "metrics." + db;
        metricsName = db + "." + metricsName;
        logger.info("ZJP.RedisMetricsImpl.addMetrics: tag={}, metricsName={}", tag, metricsName);

        try {
            String metricsId = metricsName + "_" + redisClient.incr(tag, metricsName + "_seq");
            logger.info("ZJP.RedisMetricsImpl.addMetrics: metricsName={} key={} value={}", metricsName, metricsId, value);
            Map<String, String> fields = new HashMap<>();
            if (tags != null) {
                tags.forEach((key, value1) -> fields.put(key, value1.toString()));
            }
            fields.put("value", value.toString());
            Long ts = System.currentTimeMillis();
            fields.put("ts", ts.toString());

            RedisPipeline pipeline = redisClient.getPipeline(tag);
            // tagged pipeline will add tag to metricsname automaticlly
            pipeline.hmset(metricsId, fields);
            // 设置过期时间
            pipeline.expire(metricsId, expireSeconds);
            pipeline.expire(metricsName+ "_seq", expireSeconds);
            pipeline.expire(metricsName, expireSeconds);
            // 添加到排序set, metricsName key ts 时间, set 里面的key metricsId
            pipeline.zadd(metricsName, ts, metricsId);
            // 删除掉超过时间的key(排过序的)
            pipeline.zremrangeByScore(metricsName, 0, ts - expireSeconds * 1000);
            pipeline.sync();
            // close if no longer use this pipeline
            pipeline.close();
        } catch(Exception e) {
            logger.error(String.format("redis:error in writing metrics, name: %s, tags: %s, value: %f, expire seconds: %d", metricsName, tags.toString(), value, expireSeconds), e);
        }
    }

    public List<LegendData> query(String db, String metricsName, String aggregationType, long timeStart, long timeEnd, int timeInterval, Map<String, List<Object>> filterTags, String... groupTags) {
        List<Map<String, String>> results;
        metricsName = db + "." + metricsName;
        String metricsTag = "metrics." + db;
        logger.info("ZJP.RedisMetricsImpl.query: metricsTag={}, metricsName={}", metricsTag, metricsName);
        try {
            results = getRedisRowMetrics(metricsTag, metricsName, timeStart, timeEnd - 1, filterTags);
        } catch (Exception e) {
            logger.error("redis:error in retriving row metrics from redis", e);
            return null;
        }

        if (results.size() <= 0) {
            return new ArrayList<>();
        }

        long tsBase = Long.parseLong(results.get(0).get("ts"));
        long tsStart = tsBase - tsBase % (timeInterval*1000);
        Map<String, RedisLegendData> legendDataMap = getMergedQueryRows(results, metricsName, tsStart, timeInterval, aggregationType, groupTags);

        List<LegendData> legendDatas = new ArrayList<>();
        for (RedisLegendData legendData : legendDataMap.values()) {
            if (aggregationType.equals("mean")) {
                Map<Long, AtomicInteger> tsCountMap = legendData.getTsCountMap();
                for (Map.Entry<Long, Double> entry : legendData.getTsValues().entrySet()) {
                    Long ts = entry.getKey();
                    legendData.getTsValues().put(ts, entry.getValue()/tsCountMap.get(ts).get());
                }
            }
            legendDatas.add(legendData);
        }
        return legendDatas;
    }

    private Map<String, RedisLegendData> getMergedQueryRows(List<Map<String, String>> results, String metricsName, long tsStart, int groupInterval, String aggregation, String[] groupTags) {
        Map<String, RedisLegendData> legendDataMap = new HashMap<>();
        for (Map<String, String> result : results) {
            String legend = metricsName;
            if (groupTags != null) {
                for (String tag : groupTags) {
                    legend += "." + result.get(tag);
                }
            }

            // redis need to align time manually
            long timePeriod = Long.parseLong(result.get("ts")) - tsStart;
            long groupMillis = groupInterval * 1000;
            Long ts = timePeriod/groupMillis * groupMillis + tsStart;
            if (!legendDataMap.containsKey(legend)) {
                RedisLegendData newLegendData = new RedisLegendData();
                Map<String, Object> legends = new HashMap<>();
                for (String tag : groupTags) {
                    legends.put(tag, result.get(tag));
                }
                newLegendData.setLegend(legends);
                Map<Long, Double> tsValues = new HashMap<>();
                newLegendData.setTsValues(tsValues);

                Map<Long, AtomicInteger> tsCountMap = new HashMap<>();
                newLegendData.setTsCountMap(tsCountMap);

                legendDataMap.put(legend, newLegendData);
            }

            RedisLegendData legendData = legendDataMap.get(legend);

            Map<Long, Double> tsValues = legendData.getTsValues();
            Double value = getRowData(tsValues.get(ts), Double.parseDouble(result.get("value")), aggregation);
            tsValues.put(ts, value);

            AtomicInteger count = legendData.getTsCountMap().get(ts);
            if (count == null) {
                count = new AtomicInteger();
                legendData.getTsCountMap().put(ts, count);
            }
            count.incrementAndGet();
        }
        return legendDataMap;
    }

    private List<Map<String, String>> getRedisRowMetrics(String tag, String metricsName, long timeStart, long timeEnd, Map<String, List<Object>> tags) {
        List<Map<String, String>> results = new ArrayList<>();
        Set<String> needRange = redisClient.zrangeByScore(tag, metricsName, timeStart, timeEnd);
        RedisPipeline pipeline = redisClient.getPipeline(tag);

        for (String metrics_seq : needRange) {
            pipeline.hgetAll(metrics_seq);
        }
        for (Object map : pipeline.syncAndReturnAll()) {
            Map<String, String> result = (Map<String, String>)map;

            if (result.size() <= 0) {
                continue;
            }

            boolean needAdd = true;
            // filter the results by tags...
            if (tags != null) {
                for (Map.Entry<String, List<Object>> metricsTag : tags.entrySet()) {
                    if (!metricsTag.getValue().contains(result.get(metricsTag.getKey()))) {
                        needAdd = false;
                    }
                    if (!needAdd) {
                        break;
                    }
                }
            }
            if (needAdd) {
                results.add(result);
            }
        }
        try {
            pipeline.close();
        } catch (IOException e) {
            ;
        }

        return results;
    }

    private Double getRowData(Double origin, Double current, String aggregation) {
        if (origin == null) {
            return current;
        } else if (aggregation.equals("max")) {
            return origin > current ? origin : current;
        } else if (aggregation.equals("min")) {
            return origin < current ? origin : current;
        }
        return origin + current;
    }
}
