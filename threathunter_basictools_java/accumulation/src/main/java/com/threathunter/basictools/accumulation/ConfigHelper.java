package com.threathunter.basictools.accumulation;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * created by www.threathunter.cn
 */
public class ConfigHelper {
    private static ConfigHelper helper = new ConfigHelper();

//    private String config = "config";
    private ResourceBundle resourceBundle;

    public static ConfigHelper getInstance() {
        return helper;
    }

    public void setConfig(String conf) {
        File file = new File(conf);
        try {
            URL[] urls = {file.toURI().toURL()};
            ClassLoader loader = new URLClassLoader(urls);
            resourceBundle = ResourceBundle.getBundle("config", Locale.getDefault(), loader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMysqlTableName() {
        return resourceBundle.getString("table");
    }

    public String getMysqlUrl() {
        return resourceBundle.getString("mysql");
    }

    public String getMysqlUser() {
        return resourceBundle.getString("mysqluser");
    }

    public String getMysqlPassword() {
        return resourceBundle.getString("mysqlpass");
    }

    public String getInfluxdbUrl() {
        return resourceBundle.getString("influxdb");
    }

    public String getInfluxdbUser() {
        return resourceBundle.getString("influxdbuser");
    }

    public String getInfluxdbPass() {
        return resourceBundle.getString("influxdbpass");
    }

    public int getResizeFactor() {
        return Integer.parseInt(resourceBundle.getString("resizefactor"));
    }

    public String getJsonUrl() {
        return resourceBundle.getString("jsonurl");
    }

    public Long getAccumulationPeriod() {
        return Long.parseLong(resourceBundle.getString("accumulation_period"));
    }

    public Long getLocationTopoStartTimestamp() {
        return Long.parseLong(resourceBundle.getString("topo_start_time"));
    }

    public Long getLocationTopoPeriod() {
        return Long.parseLong(resourceBundle.getString("topo_period"));
    }
    public Long getLocationTopoUpdatePeriod() {
        return Long.parseLong(resourceBundle.getString("topo_update_period"));
    }

    public String getESIp() {
        return resourceBundle.getString("es_host");
    }

    public int getESPort() {
        return Integer.parseInt(resourceBundle.getString("es_port"));
    }

    public String getIndexPrefix() {
        return resourceBundle.getString("index_prefix");
    }

    public String getTypes() {
        return resourceBundle.getString("types");
    }

    public Double getTotalFactor() {
        return Double.parseDouble(resourceBundle.getString("total_factor"));
    }

    public Double getRiskFactor() {
        return Double.parseDouble(resourceBundle.getString("risk_factor"));
    }
}
