package com.threathunter.metrics.aggregator;

import com.threathunter.metrics.MetricsAgent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 */
public class MetricsAggregator {
    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private int intervalInSeconds;
    private volatile ConcurrentHashMap<String, AggregateCounter> counterMap = new ConcurrentHashMap<>();

    public void initial(int intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
    }

    public void startAggregator() {
        scheduledExecutorService.scheduleAtFixedRate(() -> flushAggregatesData(), intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
        registerShutdownHook();
        System.out.println("aggregator started");
    }

    public void stopAggregator() {
        scheduledExecutorService.shutdown();
        flushAggregatesData();
    }

    public void add(String db, String metricsName, Map<String, Object> tags, Double value, int expireSeconds) {
        String aggregationKey = getAggregationKey(db, metricsName, tags);

        AggregateCounter container;
        // There maybe null in this way when flush data and then get from counterMap
        // This way may loss some data, but will not get exception
        if (!counterMap.containsKey(aggregationKey)) {
            counterMap.putIfAbsent(aggregationKey, new AggregateCounter(db, metricsName, tags, expireSeconds));
        }
        container = counterMap.get(aggregationKey);
        // Check null in case flush
        if (container != null) {
            container.add(value);
        }
        // DO not use this way cause we don't need to new a counter every time
//        counterMap.putIfAbsent(aggregationKey, new AggregateCounter(db, metricsName, tags, expireSeconds)).add(value);
    }

    private String getAggregationKey(String db, String metricsName, Map<String, Object> tags) {
        return String.format("%s;%s;%s", db, metricsName, tags.toString());
    }

    private void flushAggregatesData() {
        ConcurrentHashMap<String, AggregateCounter> countersMap = counterMap;
        counterMap = new ConcurrentHashMap<>();
        for (AggregateCounter counter : countersMap.values()) {
            MetricsAgent.getInstance().addMetrics(counter.getDb(), counter.getMetricsName(), counter.getTags(), counter.getValue(), counter.getExpireSeconds());
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopAggregator();
            }
        });
    }
}
