package com.threathunter.config;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicURLConfiguration;
import com.netflix.config.util.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * This config tool is for common share config management.
 * Features include:
 * 1. add config by classpath-included filename, or url
 * 2. add config dynamically and anywhere you need.
 * 3. customer defined config update frequency, default is 10s
 * 4. priority level, the config that is added later has the higher priority, will override former config's field.
 *
 * @author daisy
 */
public class CommonDynamicConfig implements CommonConfig {
    private static final Logger logger = LoggerFactory.getLogger(CommonDynamicConfig.class);
    private static final CommonDynamicConfig commonDynamicConfig = new CommonDynamicConfig();
    private final ConcurrentCompositeConfiguration compositeConfiguration = new ConcurrentCompositeConfiguration();

    private CommonDynamicConfig() {
        DynamicPropertyFactory.initWithConfigurationSource(compositeConfiguration);
    }

    public static CommonDynamicConfig getInstance() {
        return commonDynamicConfig;
    }

    public Object getProperty(String key) {
        return this.compositeConfiguration.getProperty(key);
    }

    @Override
    public String getString(String key) {
        return this.compositeConfiguration.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return this.compositeConfiguration.getString(key, defaultValue);
    }

    @Override
    public String[] getStringArray(String key) {
        return this.compositeConfiguration.getStringArray(key);
    }

    @Override
    public boolean containsKey(String key) {
        return this.compositeConfiguration.containsKey(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return this.compositeConfiguration.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return this.compositeConfiguration.getBoolean(key, defaultValue);
    }

    @Override
    public int getInt(String key) {
        return this.compositeConfiguration.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return this.compositeConfiguration.getInt(key, defaultValue);
    }

    @Override
    public long getLong(String key) {
        return this.compositeConfiguration.getLong(key);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return this.compositeConfiguration.getLong(key, defaultValue);
    }

    public void addPropertyCallback(String propertyName, Runnable r) {
        DynamicProperty property = DynamicProperty.getInstance(propertyName);
        property.addCallback(r);
    }

    public void addConfigFile(String fileName) {
        addConfigFiles(30, TimeUnit.SECONDS, fileName);
    }

    public void addConfigFile(int updatePeriod, TimeUnit unit, String fileName) {
        addConfigFiles(updatePeriod, unit, fileName);
    }

    public void addConfigFiles(String... fileNames) {
        addConfigFiles(30, TimeUnit.SECONDS, fileNames);
    }

    public void addConfigFiles(int updatePeriod, TimeUnit unit, String... fileNames) {
        String[] urls = ConfigUtils.getFileUrl(fileNames);
        if (urls != null && urls.length > 0) {
            try {
                this.compositeConfiguration.addConfigurationAtFront(new DynamicURLConfiguration(0, (int) unit.toMillis(updatePeriod),
                        false, urls), null);
            } catch (Exception e) {
                logger.error("unable to add config files: " + Arrays.toString(fileNames), e);
            }
        } else {
            logger.error("files are not found: " + Arrays.toString(fileNames));
        }
    }

    public void addConfigUrl(String url) {
        addConfigUrls(30, TimeUnit.SECONDS, url);
    }

    public void addConfigUrl(int updatePeriod, TimeUnit unit, String url) {
        addConfigUrls(updatePeriod, unit, url);
    }

    public void addConfigUrls(String... urls) {
        addConfigUrls(30, TimeUnit.SECONDS, urls);
    }

    public void addConfigUrls(int updatePeriod, TimeUnit unit, String... urls) {
        if (urls.length > 0) {
            try {
                this.compositeConfiguration.addConfigurationAtFront(
                        new DynamicURLConfiguration(0, (int) unit.toMillis(updatePeriod), false, urls), null);
            } catch (Exception e) {
                logger.error("unable to add config urls: " + Arrays.toString(urls), e);
            }
        }
    }

    public void addProperty(String key, Object value) {
        this.compositeConfiguration.addProperty(key, value);
    }

    public void addOverrideProperty(String key, Object value) {
        this.compositeConfiguration.setOverrideProperty(key, value);
    }

    public void addOverridePropertyByFile(String fileName) {
        String[] filePaths = ConfigUtils.getFileUrl(fileName);
        if (filePaths.length > 0) {
            try {
                URL url = new URL(filePaths[0]);
                InputStream fin = url.openStream();
                Properties props = ConfigurationUtils.loadPropertiesFromInputStream(fin);
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    this.addOverrideProperty((String) entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                logger.error("unable to add config file: " + fileName, e);
            }
        } else {
            logger.error("cannot find file: " + fileName);
        }
    }

    public void addOverridePropertyByUrl(String url) {
        try {
            URL u = new URL(url);
            InputStream fin = u.openStream();
            Properties props = ConfigurationUtils.loadPropertiesFromInputStream(fin);
            if (props.size() <= 0) {
                logger.error("url content is empty: " + url);
            }
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                this.addOverrideProperty((String) entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            logger.error("unable to add config file: " + url, e);
        }
    }
}
