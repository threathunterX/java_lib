package com.threathunter.basictools.rmqmetrics;

import com.threathunter.config.CommonDynamicConfig;
import com.threathunter.metrics.MetricsAgent;

import javax.json.JsonNumber;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by daisy on 2015/9/10.
 */
public class RmqMetricsWriter {
    private final int expireSeconds = 60 * 60 * 24 * 7;
    private MetricsAgent agent;
    private ScheduledExecutorService scheduledExecutorService;
    private RmqMetricsGetter metricsGetter;
    private int queryIntervalInSeconds;
    String rmq_host;
    int rmq_port;
    String rmq_username;
    String rmq_password;
    String rmq_dc;

    public RmqMetricsWriter() {
        initial();

        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        agent = MetricsAgent.getInstance();
        metricsGetter = new RmqMetricsGetter();
    }

    public void startWrite() {
        agent.start();
        scheduledExecutorService.scheduleAtFixedRate(() -> getAndWriteRmqMetics(this.rmq_host, this.rmq_port,
                this.rmq_username, this.rmq_password), 0, queryIntervalInSeconds, TimeUnit.SECONDS);
    }

    public void stopWrite() {
        scheduledExecutorService.shutdown();
        agent.stop();
    }

    private static void ensureConfigProperty(String key) {
        if (!CommonDynamicConfig.getInstance().containsKey(key)) {
            throw new RuntimeException(String.format("config property does not exist: %s", key));
        }
    }

    private void initial() {
        ensureConfigProperty("rmq_dc");
        ensureConfigProperty("rmq_username");
        ensureConfigProperty("rmq_password");
        this.rmq_dc = CommonDynamicConfig.getInstance().getString("rmq_dc");
        this.rmq_host = CommonDynamicConfig.getInstance().getString("rmq_host", "127.0.0.1");
        this.rmq_port = CommonDynamicConfig.getInstance().getInt("rmq_port", 15672);
        this.rmq_username = CommonDynamicConfig.getInstance().getString("rmq_username");
        this.rmq_password = CommonDynamicConfig.getInstance().getString("rmq_password");
        this.queryIntervalInSeconds = CommonDynamicConfig.getInstance().getInt("rmq_metrics_interval_seconds", 60);


    }

    private void getAndWriteRmqMetics(String host, int port, String username, String password) {
        try {
            writeOverviewMetrics(metricsGetter.getRmqOverviewStats(host, port, username, password));
            writeExchangeMetrics(metricsGetter.getAllExchangeStats(host, port, username, password));
            writeQueueMetrics(metricsGetter.getAllQueueStats(host, port, username, password));
        } catch (Exception e) {
            e.printStackTrace();
            writeErrorMetrics(e);
        }
    }

    private void writeExchangeMetrics(Map<String, Map<String, Object>> allExchangeStats) {
        try {
            for (Map.Entry<String, Map<String, Object>> entry : allExchangeStats.entrySet()) {
                Map<String, Object> values = entry.getValue();
                // Previously this will ignore exchanges such like amp.XXX
                // Now we have skipped all the non-manuel created exchanges.
//                if (!values.containsKey("publish_in")) {
//                    continue;
//                }
                Map<String, Object> tags = new HashMap<>();
                tags.put("vhost", values.get("vhost"));
                tags.put("durable", values.get("durable"));
                tags.put("auto_delete", values.get("auto_delete"));
                tags.put("type", values.get("type"));
                String[] splited = entry.getKey().split("\\.");
                tags.put("name", splited[1]);
                if (splited[0].equals(rmq_dc)) {
                    tags.put("local", true);
                } else {
                    tags.put("local", false);
                }
                tags.put("dc", splited[0]);
                agent.addMetrics("fx", "babel.rmq.exchange.publish_out_rate", tags, ((JsonNumber) values.get("publish_out_rate")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.exchange.publish_in_rate", tags, ((JsonNumber) values.get("publish_in_rate")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.exchange.publish_in_total", tags, ((JsonNumber) values.get("publish_in")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.exchange.publish_out_total", tags, ((JsonNumber) values.get("publish_out")).doubleValue(), expireSeconds);
            }
        } catch (Exception e) {
            writeErrorMetrics(e);
        }
    }

    private void writeErrorMetrics(Exception e) {
        Map<String, Object> map = new HashMap<>();
        map.put("exception", e.toString());
        map.put("message", e.getMessage());
        agent.addMetrics("fx", "babel.rmq.error", map, 1.0, expireSeconds);
    }

    private void writeQueueMetrics(Map<String, Map<String, Object>> allQueueStats) {
        try {
            for (Map.Entry<String, Map<String, Object>> entry : allQueueStats.entrySet()) {
                Map<String, Object> values = entry.getValue();
                Map<String, Object> tags = new HashMap<>();
                tags.put("vhost", values.get("vhost"));
                tags.put("durable", values.get("durable"));
                tags.put("auto_delete", values.get("auto_delete"));

//                tags.put("service", values.get("service"));
//                if (values.containsKey("subname")) {
//                    tags.put("subname", values.get("subname"));
//                }
                tags.put("is_idle", values.get("is_idle"));
                tags.put("status", values.get("status"));
                tags.put("dc", rmq_dc);

                String[] splited = values.get("name").toString().split("\\.");
                if (splited[0].equals("_client")) {
                    tags.put("type", "client");
                    tags.put("name", splited[1]);
                } else if (splited[0].startsWith("federation:")) {
                    // Queue: feder is remote not local
                    tags.put("type", "feder");
                    tags.put("name", splited[1].split(" ")[0]);
                    tags.put("rdc", splited[0].substring(11));
                } else {
                    tags.put("type", "service");
                    tags.put("name", splited[0]);
                    if (splited.length > 1) {
                        tags.put("subname", splited[1]);
                    }
                }

                agent.addMetrics("fx", "babel.rmq.queue.messages_total_rate", tags, ((JsonNumber)values.get("messages_rate")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.messages_ready_rate", tags, ((JsonNumber)values.get("messages_ready_rate")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.messages_unacknowledged_rate", tags, ((JsonNumber)values.get("messages_unacknowledged_rate")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.messages_total", tags, ((JsonNumber) values.get("messages")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.messages_ready", tags, ((JsonNumber) values.get("messages_ready")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.messages_unacknowledged", tags, ((JsonNumber) values.get("messages_unacknowledged")).doubleValue(), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.memory", tags, ((JsonNumber) values.get("memory")).doubleValue(), expireSeconds);

                agent.addMetrics("fx", "babel.rmq.queue.message_mb", tags, (Double) values.get("message_mb"), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.message_mb_ready", tags, (Double) values.get("message_mb_ready"), expireSeconds);
                agent.addMetrics("fx", "babel.rmq.queue.message_mb_unacknowledged", tags, (Double) values.get("message_mb_unacknowledged"), expireSeconds);
                if (values.containsKey("deliver_get_total")) {
                    // consume include deliver and get...
                    agent.addMetrics("fx", "babel.rmq.queue.consume_total", tags, ((JsonNumber) values.get("deliver_get_total")).doubleValue(), expireSeconds);
                    agent.addMetrics("fx", "babel.rmq.queue.publish_total", tags, ((JsonNumber) values.get("publish")).doubleValue(), expireSeconds);
                    agent.addMetrics("fx", "babel.rmq.queue.consume_rate", tags, ((JsonNumber) values.get("deliver_get_rate")).doubleValue(), expireSeconds);
                    agent.addMetrics("fx", "babel.rmq.queue.publish_rate", tags, ((JsonNumber) values.get("publish_rate")).doubleValue(), expireSeconds);
                }
            }
        } catch (Exception e) {
            writeErrorMetrics(e);
        }
    }

    private void writeOverviewMetrics(Map<String, Object> rmqOverviewStats) {
        Map<String, Object> tags = new HashMap<>();
        tags.put("cluster", rmqOverviewStats.get("cluster_name"));
        tags.put("version", rmqOverviewStats.get("rabbitmq_version"));
        agent.addMetrics("fx", "babel.rmq.connection_count", tags, ((JsonNumber)rmqOverviewStats.get("total_connections")).doubleValue(), expireSeconds);
        agent.addMetrics("fx", "babel.rmq.queue_count", tags, ((JsonNumber) rmqOverviewStats.get("total_queues")).doubleValue(), expireSeconds);
        agent.addMetrics("fx", "babel.rmq.exchange_count", tags, ((JsonNumber) rmqOverviewStats.get("total_exchanges")).doubleValue(), expireSeconds);
    }

//    private class ClusterInfo {
//        String ip;
//        int port;
//        String username;
//        String password;
//        ClusterInfo(String ip, int port, String username, String password) {
//            this.ip = ip;
//            this.port = port;
//            this.username = username;
//            this.password = password;
//        }
//    }
}
