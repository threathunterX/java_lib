package com.threathunter.metricsagent;

import com.threathunter.config.CommonDynamicConfig;
import com.threathunter.metrics.MetricsAgent;
import com.threathunter.metrics.model.LegendData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class TestMetricsAgent {

    @BeforeClass
    public static void setUp() {
//        CommonDynamicConfig.getInstance().addConfigFile("metrics.conf");
        CommonDynamicConfig.getInstance().addOverrideProperty("redis_cluster", "172.16.10.65:6380,172.16.10.65:6381,172.16.10.65:6382");
        CommonDynamicConfig.getInstance().addOverrideProperty("redis_password", "threathunter.cn");
        CommonDynamicConfig.getInstance().addOverrideProperty("metrics_server", "redis");
        MetricsAgent.getInstance().start();
//        MetricsAgent.getInstance().start("metricsConf.yml");
    }

    @Test
    public void testWrite() {
        for (int i = 0; i < 10; i++) {
            Car car = new Car();
            car.fillSelf();
            MetricsAgent.getInstance().addMetrics("fx_test", "car", car.getTags(), car.getPrice(), 60 * 60);
        }
    }


    @Test
    public void testQuery() {
        Map<String, List<Object>> filterTags = new HashMap<>();
        List<Object> countries = new ArrayList<>();
        countries.add("America");
        countries.add("China");
        filterTags.put("country", countries);

        long startTime = System.currentTimeMillis() / 3600000 * 3600000;
        long endTime = startTime + 3600000;

        for (LegendData legendData : MetricsAgent.getInstance().query("fx_test", "car", "mean", startTime, endTime, 60 * 5, filterTags, "level", "country")) {
            System.out.println(legendData.toString());
        }
    }

    @AfterClass
    public static void tearDown() {
        MetricsAgent.getInstance().stop();
    }
}
