package com.threathunter.babel.util;

import com.threathunter.metrics.aggregator.MetricsAggregator;

import java.util.Map;

/**
 * Created by daisy on 2015/9/2.
 */
public class BabelMetricsHelper {
    private static BabelMetricsHelper babelMetricsHelper = new BabelMetricsHelper();

    private MetricsAggregator aggregator;
    private int expireSeconds;

    private BabelMetricsHelper() {
        aggregator = new MetricsAggregator();
        aggregator.initial(60);
        expireSeconds = 60 * 60 * 24 * 7;
        aggregator.startAggregator();

        Runtime.getRuntime().addShutdownHook(new Thread(()->aggregator.stopAggregator()));
    }

    public static BabelMetricsHelper getInstance() {
        return babelMetricsHelper;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public void addMetrics(String db, String metricsName, Map<String, Object> tags, Double value) {
        aggregator.add(db, metricsName, tags, value, expireSeconds);
    }

    public String getRangeLabel(long cost) {
        if (cost < 2) {
            return "<2ms";
        } else if (cost < 5) {
            return "2ms-5ms";
        } else if (cost < 20) {
            return "5ms-20ms";
        } else if (cost < 100) {
            return "20ms-100ms";
        } else if (cost < 1000) {
            return "100ms-1s";
        } else if (cost < 5000) {
            return "1s-5s";
        } else {
            return ">5s";
        }
    }
}
