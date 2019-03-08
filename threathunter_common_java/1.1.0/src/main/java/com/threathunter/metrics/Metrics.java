package com.threathunter.metrics;


import com.threathunter.metrics.model.LegendData;

import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 2015/7/29.
 */
public interface Metrics {

    void addMetrics(String db, String metricsName, Map<String, Object> tags, Double value, int expireSeconds);

    List<LegendData> query(String db, String metricsName, String aggregationType,long timeStart, long timeEnd, int timeInterval, Map<String, List<Object>> filterTags, String... groupTags);
}
