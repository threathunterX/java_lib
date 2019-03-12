package com.threathunter.metrics.util;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.influxdb.InfluxDB;

/**
 * created by www.threathunter.cn
 */
public class InfluxdbPool extends Pool<InfluxDB> {

    public InfluxdbPool(String url, String username, String password) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxWaitMillis(5000);
        this.internalPool = new GenericObjectPool<InfluxDB>(new InfluxdbFactory(url, username, password));
    }

    public InfluxdbPool(GenericObjectPoolConfig config, String url, String username, String password) {
        this.internalPool = new GenericObjectPool<InfluxDB>(new InfluxdbFactory(url, username, password), config);
    }
}
