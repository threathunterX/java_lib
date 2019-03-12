package com.threathunter.babel.rmq;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * created by www.threathunter.cn
 */
public class RmqUtil {
    private static ThreadLocal<MessageDigest> md5 = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    public static long getHash(String key) {
        MessageDigest m = md5.get();
        m.reset();
        m.update(key.getBytes());
        byte[] bKey = m.digest();
        long res = (long)(bKey[3] & 255) << 24 | (long)(bKey[2] & 255) << 16 | (long)(bKey[1] & 255) << 8 | (long)(bKey[0] & 255);
        return res;
    }
}
