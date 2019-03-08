package com.threathunter.basictools.eslogger.util;

import com.threathunter.metrics.MetricsAgent;
import com.threathunter.metrics.model.LegendData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 2015/9/18.
 */
public class DiskUsageChecker {
    private String metricsDb;
    private String metricsName;
    private double threshold;

    private Map<String, List<Object>> filterMap;

    public DiskUsageChecker(String metricsDb, String metricsName, double threshold, String ip) {
        this.metricsDb = metricsDb;
        this.metricsName = metricsName;
        this.threshold = threshold;

        this.filterMap = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add(ip);
        this.filterMap.put("ip", list);
    }

    public boolean ifDiskWritable() {
        try {
            long currentTime = System.currentTimeMillis();
            List<LegendData> legends = MetricsAgent.getInstance().query(metricsDb, metricsName, "first", currentTime - 2 * 60 * 1000, currentTime, 1, this.filterMap);
            if (legends != null && legends.size() > 0) {
                LegendData legendData = legends.get(0);
                for (Map.Entry<Long, Double> entry : legendData.getTsValues().entrySet()) {
                    if (entry.getValue() >= threshold) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
