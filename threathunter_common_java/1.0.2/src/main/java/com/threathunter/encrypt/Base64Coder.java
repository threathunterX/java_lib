package com.threathunter.encrypt;


import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by yy on 17-9-13.
 */
public class Base64Coder {

    public String encode(String plainText) throws UnsupportedEncodingException {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Base64 encode:plain text is illeagal");
        }
        byte[] plainBytes = plainText.getBytes("utf-8");
        byte[] encodeBytes = Base64.encodeBase64(plainBytes);
        return new String(encodeBytes, "utf-8");
    }

    public String decode(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || encodeText.isEmpty()) {
            throw new IllegalArgumentException("Base64 decode:encode text is illeagal");
        }
        byte[] encodeBytes = encodeText.getBytes("utf-8");
        byte[] plainBytes = Base64.decodeBase64(encodeBytes);
        return new String(plainBytes, "utf-8");
    }

}
