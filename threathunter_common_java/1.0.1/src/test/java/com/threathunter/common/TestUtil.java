package com.threathunter.common;

import com.threathunter.metrics.MetricsAgent;
import com.threathunter.metrics.model.LegendData;
import org.junit.Test;

import static com.threathunter.common.Utility.getLocalIPAddress;

/**
 * created by www.threathunter.cn
 */
public class TestUtil {

    @Test
    public void testLocalIP() {
        System.out.println(getLocalIPAddress());
    }

    @Test
    public void testMetricsQuery() {
        MetricsAgent.getInstance().start("/home/daisy/MYCODE/java_fx/threathunter.basictools/eslogger/src/main/resources/metrics.yml");
        for(LegendData row : MetricsAgent.getInstance().query("fx_test", "car", "sum", 1453852800000l, 1453939200000l, 3600, null, "level")) {
            System.out.println(row.toString());
        }
    }
}
