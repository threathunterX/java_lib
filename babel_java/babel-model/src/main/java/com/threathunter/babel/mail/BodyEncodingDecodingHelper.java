package com.threathunter.babel.mail;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Helper class for converting string from/to Object/Binary data.
 *
 * created by www.threathunter.cn
 */
public class BodyEncodingDecodingHelper {

    private static final Logger logger = LoggerFactory.getLogger(BodyEncodingDecodingHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String encodeSingleObjectByJson(Object o) {
        if (o == null) {
            return "";
        }

        try {
            return mapper.writer().writeValueAsString(o);
        } catch (IOException e) {
            logger.error("data:fail to encode " + o.getClass(), e);
            return "";
        }
    }

    public static String encodeObjectListByJson(List<? extends Object> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }

        try {
            return mapper.writer().writeValueAsString(list);
        } catch (IOException e) {
            logger.error("data:fail to encode list of " + list.get(0).getClass(), e);
            return "";
        }
    }

    public static String encodeBinaryDataByJson(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        try {
            return new String(data, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            logger.error("data:fail to deal with the binary data", e);
            return "";
        }
    }

    public static <T> T decodeSingleObjectByJson(String data, Class<T> cls) {
        if (data == null || cls == null) {
            throw new IllegalArgumentException("data:null data or cls info");
        }

        try {
            return mapper.reader(cls).readValue(data);
        } catch (IOException e) {
            logger.error("data:fail to decode for " + cls.getName(), e);
            return null;
        }
    }

    public static <T> List<T> decodeObjectListByJson(String data, Class<T> cls) {
        if (data == null || cls == null) {
            throw new IllegalArgumentException("null data or cls info");
        }

        try {
            return mapper.reader(new TypeReference<List<T>>() {}).readValue(data);
        } catch (IOException e) {
            logger.error("data:fail to decode for list of " + cls.getName(), e);
            return null;
        }
    }

    public static byte[] decodeBinaryDataByJson(String data) {
        if (data == null) {
            return new byte[0];
        }

        try {
            return data.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            logger.error("data:fail to encode to iso-8859-1", e);
            return new byte[0];
        }
    }
}
