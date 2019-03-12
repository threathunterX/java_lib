package com.threathunter.config;

/**
 * created by www.threathunter.cn
 */
public interface CommonConfig {
    String getString(String key);

    String getString(String key, String defaultValue);

    int getInt(String key);

    int getInt(String key, int defaultValue);

    long getLong(String key);

    long getLong(String key, long defaultValue);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    String[] getStringArray(String key);

    boolean containsKey(String key);
}
