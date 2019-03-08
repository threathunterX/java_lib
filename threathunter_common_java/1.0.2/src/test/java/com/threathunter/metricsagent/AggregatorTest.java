package com.threathunter.metricsagent;

import com.threathunter.metrics.MetricsAgent;
import com.threathunter.metrics.aggregator.MetricsAggregator;
import com.threathunter.metrics.model.LegendData;
import org.junit.Test;

import java.util.*;

/**
 * Created by daisy on 2015/8/27.
 */
public class AggregatorTest {
    MetricsAggregator aggregator = new MetricsAggregator();

    @Test
    public void testQuery() {
        //objj=mt.query(‘webui’,’metrics_proxy_test’,’mean’,starttime*1000,endtime*1000,10,{},’’)
        MetricsAgent.getInstance().start("metrics.yml");
        List<LegendData> rows = MetricsAgent.getInstance().query("webui", "metrics_proxy_test", "mean", 1453717794955l, 1453718160946l, 10, null, "name");
        if (rows != null) {
            for (LegendData row : rows) {
                System.out.println(row.toString());
            }
        } else {
            System.out.println("null data");
        }
    }

    @Test
    public void testAggregator() {
        MetricsAgent.getInstance().start("metricsConf.yml");
        aggregator.initial(60);
        aggregator.startAggregator();

        for (int i = 0; i < 100; i++) {
            Car car = new Car();
            car.fillSelf();
            aggregator.add("fx_test", "car", car.getTags(), car.getPrice(), 60 * 60);
        }

        aggregator.stopAggregator();
        MetricsAgent.getInstance().stop();
    }

    @Test
    public void testAggregatorInfluxdb() throws InterruptedException {
        MetricsAgent.getInstance().start("metricsConf.yml");
        aggregator.initial(5*60);
        aggregator.startAggregator();

        String db = "fx_test";
        String metricsName = "car";

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Car car1 = new Car();
                Car car2 = new Car();
                car1.fillSelf();
                car2.fillSelf();
                for (int i = 0; i < 3; i++) {
                    aggregator.add(db, metricsName, car1.getTags(), car1.getPrice(), 60 * 60 * 24);
                    aggregator.add(db, metricsName, car2.getTags(), car2.getPrice(), 60 * 60 * 24);
                }
            }
        }, 0, 1000*60);

        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < 20 * 60 * 1000) {
            Thread.sleep(3000);
        }
        aggregator.stopAggregator();

    }

    @Test
    public void writeComplexMetrics() throws InterruptedException {
        MetricsAgent agent = MetricsAgent.getInstance();
        agent.start("MetricsDbConfig.yml");
        // name: car
        // 3 tags: type, level, country
        // value: price
        // every 2 minutes write 3 metrics
        String name = "car";
        for (int i = 0; i < 20; i++) {
            if (i > 0 && i%3 == 0) {
                Thread.sleep(2 * 60 * 1000);
            }
            Car car = new Car();
            car.fillSelf();
            agent.addMetrics("MetricsCar", name, car.getTags(), car.getPrice(), 60 * 60 * 24 * 2);
        }
    }
}
