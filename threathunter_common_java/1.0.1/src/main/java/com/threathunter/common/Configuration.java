package com.threathunter.common;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Configuration from file and resources, based on freeway version
 * User: wenlu
 * Date: 13-8-27
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private static ClassLoader classLoader;
    private final static Configuration instance = new Configuration();

    static {
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = Configuration.class.getClassLoader();
        }
    }

    public static Configuration instance() {
        return instance;
    }

    private volatile ImmutableMap<String, String> configMap = ImmutableMap.of();
    private volatile ImmutableMap<String, String> webConfigMap = ImmutableMap.of();
    public Map<String, String> getAllConfig() {
        Map<String, String> result = new HashMap<>(configMap);
        result.putAll(webConfigMap);
        return result;
    }
    private String weburl = "";
    private final String auth="40eb336d9af8c9400069270c01e78f76";

    public void addConfigResource(String name) {
        InputStream in = null;
        try {
            URL url = classLoader.getResource(name);
            if (url != null) {
                in = url.openStream();
                loadConfig(in);
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot load configuration from Resource <" + name + ">", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void addWebConfig(String weburl) {
        this.weburl = weburl;
        this.getWebConfig();
        ses.scheduleWithFixedDelay(this::getWebConfig, 0, 10, TimeUnit.SECONDS);
    }

    private String getRestfulResult() throws Exception {
        InputStream inputStream = null;
        try {
            HttpURLConnection conn = getWebURLConnection(String.format("%s?auth=%s", this.weburl, this.auth));
            inputStream = conn.getInputStream();
            return readInputStream(inputStream);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private HttpURLConnection getWebURLConnection(String url) throws Exception {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(1000 * 30);
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("content-type", "application/json");
        conn.setDoOutput(false);
        conn.setDoInput(true);
        return conn;
    }

    public void getWebConfig() {
        if (this.weburl == null || this.weburl.isEmpty()) {
            return;
        }

        try {
            String s = getRestfulResult();
            if (s == null || s.isEmpty())
                return;
            JsonObject config = new JsonParser().parse(s).getAsJsonObject();
            Map<String, String> tempMap = new HashMap<>();
            JsonArray values = config.getAsJsonArray("values");
            for (int i = 0; i < values.size(); i++) {
                JsonObject item = values.get(i).getAsJsonObject();
                String key = item.get("key").getAsString();
                String value = item.get("value").getAsString();
                tempMap.put(key, value);
            }

            this.webConfigMap = ImmutableMap.copyOf(tempMap);
        } catch (Exception e) {
            LOGGER.error("fail to get the web config", e);
        }

    }

    private String readInputStream(InputStream in) throws IOException {
        char[] buffer = new char[2000];
        StringBuilder result = new StringBuilder();
        InputStreamReader ins = new InputStreamReader(in);
        int readBytes = 0;
        while ((readBytes = ins.read(buffer, 0, 2000)) >= 0) {
            result.append(buffer, 0, readBytes);
        }
        return result.toString();
    }

    public void addConfigFile(String name) {
        InputStream in = null;
        try {
            File confFile = new File(name);
            if (confFile.isFile() == false) {
                return;
            }
            in = new FileInputStream(name);
            if (in != null) {
                loadConfig(in);
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot load configuration from file <" + name + ">", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void loadConfig(InputStream in) throws Exception {
        Map<String, String> tempMap = new HashMap<String, String>(configMap);
        Properties props = new Properties();
        props.load(in);
        Enumeration<String> en = (Enumeration<String>) props.propertyNames();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            tempMap.put(key, props.getProperty(key));
        }
        configMap = ImmutableMap.copyOf(tempMap);
    }

    public void loadConfig(Map<String, String> map) {
        Map<String, String> tempMap = new HashMap<String, String>(configMap);
        tempMap.putAll(map);
        configMap = ImmutableMap.copyOf(tempMap);
    }

    /**
     * Get the value of the name property
     *
     * @param name the property name.
     * @return the value of the name, or null if no such property exists.
     */
    public String get(String name) {
        return getTrimmed(name);
    }

    /**
     * Get the value of the name property
     *
     * @param name         the property name.
     * @param defaultValue default value.
     * @return the value of the name, or defaultValue if no such property exists.
     */
    public String get(String name, String defaultValue) {
        String result = get(name);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Get the value of the name property as a trimmed string.
     *
     * @param name the property name.
     * @return the value of the name, or defaultValue if no such property exists.
     */
    private String getTrimmed(String name) {
        String value = webConfigMap.get(name);
        if (value == null)
            value = configMap.get(name);
        if (null == value) {
            return null;
        } else {
            return value.trim();
        }
    }

    /**
     * Get the value of the name property
     *
     * @param name         the property name.
     * @param defaultValue default value.
     * @return the value of the name, or defaultValue if no such property exists.
     */
    public int getInt(String name, int defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        return Integer.parseInt(valueString);
    }

    public int getInt(String name) {
        return Integer.parseInt(get(name));
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        return Boolean.getBoolean(valueString);
    }

    public boolean getBoolean(String name) {
        return Boolean.valueOf(get(name));
    }

    public String[] getStrings(String name) {
        String valueString = get(name);
        return getTrimmedStrings(valueString);
    }

    private static String[] getTrimmedStrings(String str) {
        if (null == str || "".equals(str.trim())) {
            return new String[0];
        }

        return str.trim().split("\\s*,\\s*");
    }

    public Class<?> getClass(String name) throws ClassNotFoundException {
        String valueString = getTrimmed(name);
        if (valueString == null) {
            throw new ClassNotFoundException("Class " + name + " not found");
        }
        return Class.forName(valueString, true, classLoader);
    }

    public Class<?>[] getClasses(String name) throws ClassNotFoundException {
        String[] classNames = getStrings(name);
        if (classNames == null) {
            return null;
        }
        Class<?>[] classes = new Class<?>[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            classes[i] = getClass(classNames[i]);
        }
        return classes;
    }

    public void dumpDeprecatedKeys() {
        for (String key : configMap.keySet()) {
            System.out.println(key + "=" + configMap.get(key));
        }
    }
}
