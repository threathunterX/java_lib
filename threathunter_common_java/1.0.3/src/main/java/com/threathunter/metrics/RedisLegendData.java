package com.threathunter.metrics;


import com.threathunter.metrics.model.LegendData;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by www.threathunter.cn
 */
public class RedisLegendData extends LegendData {
    private Map<Long, AtomicInteger> tsCountMap;

    public Map<Long, AtomicInteger> getTsCountMap() {
        return tsCountMap;
    }

    public void setTsCountMap(Map<Long, AtomicInteger> tsCountMap) {
        this.tsCountMap = tsCountMap;
    }

    public String toString() {
        return String.format("legend: %s, data: %s", getLegend().toString(), getTsValues().toString());
    }
}
