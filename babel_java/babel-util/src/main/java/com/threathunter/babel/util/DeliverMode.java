package com.threathunter.babel.util;

import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public enum DeliverMode {
    QUEUE,
    SHUFFLE,
    SHARDING,
    TOPIC,
    TOPICSHUFFLE,
    TOPICSHARDING;

    private final static Map<String, DeliverMode> m = new HashMap<>();
    static {
        m.put("queue", QUEUE);
        m.put("topic", TOPIC);
        m.put("shuffle", SHUFFLE);
        m.put("sharding", SHARDING);
        m.put("topicshuffle", TOPICSHUFFLE);
        m.put("topicsharding", TOPICSHARDING);
    }

    public static DeliverMode getEnum(String name) {
        return DeliverMode.valueOf(name.toUpperCase());
    }

    public static DeliverMode fromString(String name) {
        DeliverMode result = m.get(name);
        return result;
    }

}
