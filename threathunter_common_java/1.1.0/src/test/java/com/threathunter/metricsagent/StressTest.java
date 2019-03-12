package com.threathunter.metricsagent;

import com.threathunter.metrics.MetricsAgent;
import com.threathunter.metrics.aggregator.MetricsAggregator;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * created by www.threathunter.cn
 */
public class StressTest {
    String metricsConfig = "metricsConf.yml";
    String db = "fx_test";
    MetricsAggregator aggregator = new MetricsAggregator();

    @Before
    public void setUp() {
        MetricsAgent.getInstance().start(metricsConfig);
        aggregator.initial(60);
        aggregator.startAggregator();
    }

    @Test
    public void testEsIncomeStress() throws InterruptedException {
        final String[] indexs = new String[3];
        indexs[0] = "correlation";
        indexs[1] = "honeypot";
        indexs[1] = "cia_dbs";

        final String[] types = new String[3];
        types[0] = "redq";
        types[1] = "proxy_req_log";
        types[2] = "crawler";

        Random rand = new Random();
        AtomicLong count = new AtomicLong(0);

        long timeStart = System.currentTimeMillis();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                while (System.currentTimeMillis() - timeStart <= 1000 * 60 * 5) {
                    int ind = rand.nextInt(3);
                    addRandomEsIncomeMetrics(indexs[ind], types[ind]);
                    count.incrementAndGet();
//                    if (count.incrementAndGet() % 5 == 0) {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }

                }
            });
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        aggregator.stopAggregator();
        MetricsAgent.getInstance().stop();
        System.out.println(count.get());
    }

    private void addRandomEsIncomeMetrics(String index, String type) {
        Map<String, Object> tags = new HashMap<>();
        tags.put("index", index);
        tags.put("type", type);
        aggregator.add(db, "eslogger.income", tags, 1.0, 1000 * 3600 * 24);
    }
}
