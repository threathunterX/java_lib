package com.threathunter.metrics;

import com.threathunter.config.CommonDynamicConfig;
import com.threathunter.metrics.model.LegendData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.threathunter.common.Utility.getLocalIPAddress;
import static com.threathunter.common.Utility.getParentPackageName;
import static com.threathunter.common.Utility.isListEqual;

/**
 * Created by daisy on 2015/7/29.
 */
public class MetricsAgent {
    private static final Logger logger = LoggerFactory.getLogger(MetricsAgent.class);
    private static final MetricsAgent instance = new MetricsAgent();
    private volatile boolean isRunning = false;
    private Thread worker;
    private ConcurrentLinkedQueue<MetricsHolder> metricses;
    private volatile Metrics metricsImpl;
    private volatile String app = "";

    private MetricsAgent(){}

    public static MetricsAgent getInstance() {
        return instance;
    }

    // use CommonDynamicConfig
    public void start() {
        if (isRunning) {
            logger.debug("metrics agent is running");
        }
        String server = CommonDynamicConfig.getInstance().getString("metrics_server");
        if (server == null) {
            throw new RuntimeException("config item is null: metrics_server");
        }
        if (server != null && server.equals("redis")) {
            String[] addresses = CommonDynamicConfig.getInstance().getStringArray("redis_cluster");
            String password = CommonDynamicConfig.getInstance().getString("redis_password", null);
            if (addresses == null || addresses.length <= 0) {
                String host = CommonDynamicConfig.getInstance().getString("redis_host", "127.0.0.1");
                int port = CommonDynamicConfig.getInstance().getInt("redis_port", 6379);
                metricsImpl = new RedisMetricsImpl(host, port, password);
            } else {
                metricsImpl = new RedisMetricsImpl(addresses, password);
            }
        } else if (server.equals("influxdb")) {
            String url = CommonDynamicConfig.getInstance().getString("influxdb_url", "http://127.0.0.1:8086/");
            String username = CommonDynamicConfig.getInstance().getString("influxdb_username", "root");
            String password = CommonDynamicConfig.getInstance().getString("influxdb_password");
            metricsImpl = new InfluxdbMetricsImpl(url, username, password);
        }

        app = CommonDynamicConfig.getInstance().getString("app", "");

        initialThread();
    }

    public void start(String config) {
        if (isRunning) {
            logger.debug("metrics agent is running");
        }
        try {
            Yaml yaml = new Yaml();
            InputStream is;
            try {
                is = new FileInputStream(new File(config));
            } catch (IOException ex) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
            }
            Map<String, Object> map = (Map<String, Object>) yaml.load(is);
            String server = (String) map.get("server");
            Map<String, Object> serverMap = (Map<String, Object>) map.get(server);
            if (server.equals("redis")) {
                String host = (String) serverMap.get("host");
                int port = ((Number) serverMap.get("port")).intValue();
                if (serverMap.containsKey("password")) {
                    metricsImpl = new RedisMetricsImpl(host, port, (String) serverMap.get("password"));
                } else {
                    metricsImpl = new RedisMetricsImpl(host, port);
                }
            } else if (server.equals("influxdb")) {
                metricsImpl = new InfluxdbMetricsImpl((String) serverMap.get("url"), (String)serverMap.get("username"), (String)serverMap.get("password"));
            } else {
                throw new RuntimeException("unsupported metrics server: " + server);
            }

            app = (String)map.getOrDefault("app", "");
        } catch (Exception e) {
            logger.error("init:fatal:unable to initial from config", e);
            throw new RuntimeException(e);
        }

        initialThread();
    }

    public void stop() {
        System.out.println("Agent going to stop");
        isRunning = false;
        try {
            worker.interrupt();
            worker.join();
            while (metricses.size() > 0) {
                MetricsHolder metrics = metricses.poll();
                metricsImpl.addMetrics(metrics.getDb(), metrics.getMetricsName(), metrics.getTags(), metrics.getValue(), metrics.getExpireTime());
            }
        } catch (InterruptedException e) {
            logger.error("close:error in stopping the agent", e);
        }
    }

    public void addMetrics(String db, String metricsName, Map<String, Object> tags, Double value, int expireSeconds) {
        if (metricsImpl == null) {
            throw new RuntimeException("metrics is not initialized");
        }

        if ((tags == null) || (tags == Collections.<String, Object>emptyMap())) {
            tags = new HashMap();
        }
        if (!tags.containsKey("hostip")) {
            tags.put("hostip", getLocalIPAddress());
        }
        if (!tags.containsKey("app")) {
            tags.put("app", app);
        }
        metricses.add(new MetricsHolder(db, metricsName, tags, value, expireSeconds));
    }

    public List<LegendData> query(String db, String metricsName, String aggregationType, long timeStart, long timeEnd, int timeIntervalInSec, Map<String, List<Object>> filterTags, String... groupTags) {
        if (metricsImpl == null) {
            throw new RuntimeException("metrics is not initialized");
        }
        return metricsImpl.query(db, metricsName, aggregationType, timeStart, timeEnd, timeIntervalInSec, filterTags, groupTags);
    }

    private void initialThread() {
        isRunning = true;
        metricses = new ConcurrentLinkedQueue<>();

        worker = new Thread("metrics agent worker") {
            private int emptyrun = 0;

            @Override
            public void run() {
                while (isRunning) {
                    MetricsHolder metrics = metricses.poll();
                    if (metrics == null) {
                        emptyrun++;
                        if (emptyrun >= 3) {
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < 3; i++) {
                            try {
                                metricsImpl.addMetrics(metrics.getDb(), metrics.getMetricsName(), metrics.getTags(), metrics.getValue(), metrics.getExpireTime());
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        worker.setDaemon(true);
        worker.start();
    }

    private static class MetricsHolder {
        private String db;
        private String metricsName;
        private Map<String, Object> tags;
        private Double value;
        private int expireTime;

        public MetricsHolder(String db, String metricsName, Map<String, Object> tags, Double value, int expireTime) {
            this.db = db;
            this.metricsName = metricsName;
            this.tags = tags;
            this.value = value;
            this.expireTime = expireTime;
        }

        public String getDb() {
            return db;
        }

        public void setDb(String db) {
            this.db = db;
        }

        public String getMetricsName() {
            return metricsName;
        }

        public void setMetricsName(String metricsName) {
            this.metricsName = metricsName;
        }

        public Map<String, Object> getTags() {
            return tags;
        }

        public void setTags(Map<String, Object> tags) {
            this.tags = tags;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public int getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(int expireTime) {
            this.expireTime = expireTime;
        }
    }
}
