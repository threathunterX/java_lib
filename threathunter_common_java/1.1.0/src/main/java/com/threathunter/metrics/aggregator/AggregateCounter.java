package com.threathunter.metrics.aggregator;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.Map;

/**
 * Created by daisy on 2015/8/27.
 */
public class AggregateCounter {
    private final String db;
    private final String metricsName;
    private final Map<String, Object> tags;
    private final int expireSeconds;
    private final AtomicDouble value;

    public AggregateCounter(String db, String metricsName, Map<String, Object> tags, int expireSeconds) {
        this.db = db;
        this.metricsName = metricsName;
        this.tags = tags;
        this.expireSeconds = expireSeconds;
        this.value = new AtomicDouble(0.0);
    }

    public void add(Double value) {
        this.value.addAndGet(value);
    }

    public String getDb() {
        return db;
    }

    public String getMetricsName() {
        return metricsName;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public Double getValue() {
        return value.doubleValue();
    }
}
