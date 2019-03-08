package com.threathunter.basictools.rmqmetrics;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.xml.bind.DatatypeConverter;
//import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by daisy on 2015/9/9.
 */
public class RmqMetricsGetter {

    public Map<String, Object> getRmqOverviewStats(String host, int port, String username, String password) {
        URL url;
        Map<String, Object> map;
        try {
            url = new URL(String.format("http://%s:%d/api/overview", host, port));
            map = new HashMap<>();
            URLConnection con = url.openConnection();
            String userpass = username + ":" + password;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            con.setRequestProperty("Authorization", basicAuth);
            JsonObject object = Json.createReader(con.getInputStream()).readObject();
            map.put("rabbitmq_version", object.getString("rabbitmq_version"));
            if (object.containsKey("cluster_name")) {
                map.put("cluster_name", object.getString("cluster_name"));
            } else {
                map.put("cluster_name", object.getString("node"));
            }
            Map<String, Object> object_totals = (Map<String, Object>) object.get("object_totals");
            map.put("total_queues", object_totals.get("queues"));
            map.put("total_exchanges", object_totals.get("exchanges"));
            map.put("total_connections", object_totals.get("connections"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public Map<String, Map<String, Object>> getAllExchangeStats(String host, int port, String username, String password) {
        URL url;
        Map<String, Map<String, Object>> map;
        try {
            url = new URL(String.format("http://%s:%d/api/exchanges", host, port));
            map = new HashMap<>();
            URLConnection con = url.openConnection();
            String userpass = username + ":" + password;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            con.setRequestProperty("Authorization", basicAuth);
            JsonArray array = Json.createReader(con.getInputStream()).readArray();
            for (JsonValue value : array) {
                Map<String, Object> submap = new HashMap<>();
                JsonObject object = (JsonObject)value;
                String exchangeName = object.getString("name");
                // To only get the local exchanges, no federation's exchanges and default exchanges
                String[] splited = exchangeName.split("\\.");
                if (splited.length < 2 || !splited[1].startsWith("_")) {
                    continue;
                }
                submap.put("vhost", object.getString("vhost"));
                submap.put("type", object.getString("type"));
                submap.put("durable", object.getBoolean("durable"));
                submap.put("auto_delete", object.getBoolean("auto_delete"));
                if (object.containsKey("message_stats")) {
                    Map<String, Object> stats = (Map<String, Object>) object.get("message_stats");
                    submap.put("publish_in", stats.get("publish_in"));
                    submap.put("publish_in_rate", ((Map<String, Object>) stats.get("publish_in_details")).get("rate"));
                    submap.put("publish_out", stats.get("publish_out"));
                    submap.put("publish_out_rate", ((Map<String, Object>) stats.get("publish_out_details")).get("rate"));
                }
                map.put(object.getString("name"), submap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public Map<String, Map<String, Object>> getAllQueueStats(String host, int port, String username, String password) {
        // We not only need to get service queue, but also queues for client and federation
//        Set<String> serviceQueueNames = this.getAllServiceQueueNames(host, port, username, password);
        URL url;
        Map<String, Map<String, Object>> map;
        try {
            url = new URL(String.format("http://%s:%d/api/queues", host, port));
            map = new HashMap<>();
            URLConnection con = url.openConnection();
            String userpass = username + ":" + password;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            con.setRequestProperty("Authorization", basicAuth);
            JsonArray array = Json.createReader(con.getInputStream()).readArray();
            for (JsonValue value : array) {
                Map<String, Object> submap = new HashMap<>();
                JsonObject object = (JsonObject)value;
//                if (!serviceQueueNames.contains(object.getString("name"))) {
//                    continue;
//                }
                if (object.containsKey("message_stats")) {
                    Map<String, Object> stats = (Map<String, Object>) object.get("message_stats");

                    // Since babel use consumer to wait broker to push messages, in fact there are only deliver(no get)
                    // so deliver_get is the sum of only deliver
                    // here we ignore deliver
                    if (stats.containsKey("ack")) {
                        submap.put("ack_total", stats.get("ack"));
                        submap.put("ack_rate", ((Map<String, Object>) stats.get("ack_details")).get("rate"));
                    }
                    if (stats.containsKey("deliver_get")) {
                        submap.put("deliver_get_total", stats.get("deliver_get"));
                        submap.put("deliver_get_rate", ((Map<String, Object>) stats.get("deliver_get_details")).get("rate"));
                    }
                    if (stats.containsKey("publish")) {
                        submap.put("publish", stats.get("publish"));
                        submap.put("publish_rate", ((Map<String, Object>) stats.get("publish_details")).get("rate"));
                    }
                }
                if (object.containsKey("messages")) {
                    submap.put("messages", object.get("messages"));
                    submap.put("messages_rate", ((Map<String, Object>) object.get("messages_details")).get("rate"));
                }
                if (object.containsKey("messages_ready")) {
                    submap.put("messages_ready", object.get("messages_ready"));
                    submap.put("messages_ready_rate", ((Map<String, Object>) object.get("messages_ready_details")).get("rate"));
                }
                if (object.containsKey("messages_unacknowledged")) {
                    submap.put("messages_unacknowledged", object.get("messages_unacknowledged"));
                    submap.put("messages_unacknowledged_rate", ((Map<String, Object>) object.get("messages_unacknowledged_details")).get("rate"));
                }
                submap.put("memory", object.get("memory"));
                if (object.containsKey("state")) {
                    submap.put("status", object.getString("state"));
                } else {
                    submap.put("status", object.getString("status"));
                }
                if (object.containsKey("idle_since")) {
                    submap.put("is_idle", true);
                } else {
                    submap.put("is_idle", false);
                }
                submap.put("message_mb", object.getInt("message_bytes")/1000000.0);
                submap.put("message_mb_ready", object.getInt("message_bytes_ready")/1000000.0);
                submap.put("message_mb_unacknowledged", object.getInt("message_bytes_unacknowledged")/1000000.0);

                submap.put("vhost", object.getString("vhost"));
                submap.put("durable", object.getBoolean("durable"));
                submap.put("auto_delete", object.getBoolean("auto_delete"));

                // Need to extract some service name and subname for metrics and will deal with it when write into metrics
//                String[] names = object.getString("name").split("\\.", 3);
//                if (names.length > 1) {
//                    submap.put("subname", names[1]);
//                    if (names.length > 2) {
//                        submap.put("sequence", names[2]);
//                    }
//                }
//                submap.put("service", names[0]);
                submap.put("name", object.getString("name"));

                map.put(object.getString("name"), submap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public Set<String> getAllServiceQueueNames(String host, int port, String username, String password) {
        URL url;
        Set<String> serviceQueueNames = new HashSet<>();
        try {
            url = new URL(String.format("http://%s:%d/api/bindings", host, port));
            URLConnection con = url.openConnection();
            String userpass = username + ":" + password;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            con.setRequestProperty("Authorization", basicAuth);
            JsonArray array = Json.createReader(con.getInputStream()).readArray();
            for (JsonValue jsonValue : array) {
                JsonObject object = (JsonObject)jsonValue;
                String source = object.getString("source");
                if (source.equals("") || source.equals("_client")) {
                    continue;
                }
                serviceQueueNames.add(object.getString("destination"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serviceQueueNames;
    }
}
