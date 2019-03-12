package com.threathunter.metrics.util;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

/**
 * created by www.threathunter.cn
 */
public class InfluxdbFactory implements PooledObjectFactory<InfluxDB> {
    private final String url;
    private final String username;
    private final String password;

    public InfluxdbFactory(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public PooledObject<InfluxDB> makeObject() throws Exception {
        InfluxDB influxDB = InfluxDBFactory.connect(this.url, this.username, this.password);
        return new DefaultPooledObject<InfluxDB>(influxDB);
    }

    public void destroyObject(PooledObject<InfluxDB> pooledObject) throws Exception {
    }

    public boolean validateObject(PooledObject<InfluxDB> pooledObject) {
        return false;
    }

    public void activateObject(PooledObject<InfluxDB> pooledObject) throws Exception {

    }

    public void passivateObject(PooledObject<InfluxDB> pooledObject) throws Exception {

    }
}
