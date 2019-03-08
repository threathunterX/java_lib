package com.threathunter.babel.util;

import com.threathunter.babel.meta.ServiceMeta;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 2015/6/30.
 */
public class LocalServiceRegistry {
    private static final LocalServiceRegistry instance = new LocalServiceRegistry();

    // registery should have a detailed describe of a service
    private Map<String, Map<String, Object>> servicePropertiesMap = new HashMap<>();

    // #0~#19 shard number...
    private static String[] rmqShardNumberArray = new String[20];

    static {
        try {
            initialRmqShardNumberArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalServiceRegistry getInstance() {
        return instance;
    }

    public void addServiceMeta(ServiceMeta meta) {
        if (meta.getServerImpl().equals(ServiceConstant.RMQ_SERVER_NAME)) {
        } else if (meta.getServerImpl().equals(ServiceConstant.REDIS_SERVER_NAME)) {
        } else {
            throw new RuntimeException("server implementation is not support yet: "+meta.getServerImpl());
        }
    }

    public void addServiceMetas(List<ServiceMeta> metas) {
        for (ServiceMeta meta : metas) {
            addServiceMeta(meta);
        }
    }

    public Map<String, Object> getProperties(String serviceName) {
        return servicePropertiesMap.get(serviceName);
    }

    public String getRmqShardNumber(int bucket) {
        return rmqShardNumberArray[bucket];
    }

    private static void initialRmqShardNumberArray() throws IOException {
        rmqShardNumberArray[0] = "60";
        rmqShardNumberArray[1] = "29";
        rmqShardNumberArray[2] = "84";
        rmqShardNumberArray[3] = "22";
        rmqShardNumberArray[4] = "17";
        rmqShardNumberArray[5] = "63";
        rmqShardNumberArray[6] = "18";
        rmqShardNumberArray[7] = "20";
        rmqShardNumberArray[8] = "24";
        rmqShardNumberArray[9] = "11";
        rmqShardNumberArray[10] = "64";
        rmqShardNumberArray[11] = "68";
        rmqShardNumberArray[12] = "96";
        rmqShardNumberArray[13] = "12";
        rmqShardNumberArray[14] = "62";
        rmqShardNumberArray[15] = "92";
        rmqShardNumberArray[16] = "59";
        rmqShardNumberArray[17] = "56";
        rmqShardNumberArray[18] = "14";
        rmqShardNumberArray[19] = "15";
    }
}
