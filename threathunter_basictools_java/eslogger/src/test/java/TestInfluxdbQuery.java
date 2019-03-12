import com.threathunter.metrics.MetricsAgent;
import com.threathunter.metrics.model.LegendData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class TestInfluxdbQuery {
    @Test
    public void testDiskUsageQuery() {
        String metricsConf = "/home/daisy/MYCODE/java_fx/threathunter.basictools/eslogger/src/main/resources/metrics.yml";
        MetricsAgent.getInstance().start(metricsConf);
        long currentTime = System.currentTimeMillis();
        Map<String, List<Object>> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add("172.16.0.104");
        map.put("ip", list);
        for (LegendData row : MetricsAgent.getInstance().query("monitor", "systemstats.totaldisk", "first", currentTime-60*1000, currentTime, 1, map)) {
            System.out.println(row.toString());
        }
    }
}
