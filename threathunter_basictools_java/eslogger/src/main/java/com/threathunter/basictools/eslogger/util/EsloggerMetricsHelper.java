package com.threathunter.basictools.eslogger.util;

import com.threathunter.metrics.aggregator.MetricsAggregator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class EsloggerMetricsHelper {
    private static EsloggerMetricsHelper esloggerMetricsHelper = new EsloggerMetricsHelper();

    private MetricsAggregator aggregator;
    private int expireSeconds = 60 * 60 * 24 * 7;
    private String db = "fx";

    private EsloggerMetricsHelper() {
        aggregator = new MetricsAggregator();
        aggregator.initial(60);
        aggregator.startAggregator();

        Runtime.getRuntime().addShutdownHook(new Thread(()->aggregator.stopAggregator()));
    }

    public static EsloggerMetricsHelper getInstance() {
        return esloggerMetricsHelper;
    }

    public void addIncomeLoggerMetrics(String index, String type) {
        Map<String, Object> tags = new HashMap<>();
        tags.put("index", index);
        tags.put("type", type);
        aggregator.add(db, "eslogger.income", tags, 1.0, expireSeconds);
    }

    public void addErrorMetrics(String index, String type, Exception e) {
        Map<String, Object> tags = new HashMap<>();
        tags.put("index", index);
        tags.put("type", type);
        tags.put("class", e.getClass().toString());
        tags.put("message", e.getMessage());
        aggregator.add(db, "eslogger.error", tags, 1.0, expireSeconds);
    }
}
