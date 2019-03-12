package com.threathunter.variable;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class JsonFileReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T getFromResourceFile(String file, Class<T> valueType) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        try {
            return MAPPER.readValue(is, valueType);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static List<Object> getValuesFromFile(String file, ClassType type) throws IOException {
        if (type == ClassType.MAP) {
            Map<String, Object> map = getFromResourceFile(file, Map.class);
            return (List<Object>) map.get("values");
        } else if (type == ClassType.LIST) {
            return getFromResourceFile(file, List.class);
        }
        throw new RuntimeException("cannot read from json file, object is neither a map nor a list");
    }

    public static enum ClassType {
        MAP,
        LIST
    }

}
