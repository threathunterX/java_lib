package com.threathunter.redis;

/**
 * created by www.threathunter.cn
 */
public class RedisKeyUtils {
    public static String genTaggedKey(String tag, String key) {
        return String.format("{%s}%s", tag, key);
    }

    public static String[] genTaggedKeys(String tag, String... keys) {
        String[] array = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            array[i] = genTaggedKey(tag, keys[i]);
        }
        return array;
    }

    public static String[] genTaggedKeysValues(String tag, String... keysvalues) {
        String[] array = new String[keysvalues.length];
        for (int i = 0; i < keysvalues.length; i += 2) {
            array[i] = genTaggedKey(tag, keysvalues[i]);
        }
        return array;
    }
}
