package com.threathunter.babel.rpc;

import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

/**
 * created by www.threathunter.cn
 */
public class HashUtils {
    public static String getHash(String key) {
        String hashString = "" + Hashing.murmur3_32().hashString(key, Charset.defaultCharset()).asInt();
//        System.out.println(String.format("key: %s, hash: %s", key, hashString));

        return hashString;
    }
}
