package com.threathunter.variable.meta;

import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class DelayCollectorVariableMeta extends CollectorVariableMeta {
    public static final String TYPE = "delaycollector";

    static {
        addSubClass(TYPE, DelayCollectorVariableMeta.class);
    }

    private long sleepTimeMillis;

    public long getSleepTimeMillis() {
        return sleepTimeMillis;
    }

    public static DelayCollectorVariableMeta from_json_object(Object obj) {
        DelayCollectorVariableMeta result = new DelayCollectorVariableMeta();
        result = from_json_object(obj, result);

        Map<String, Object> map = (Map<String, Object>) obj;
        Map<String, Object> function = (Map<String, Object>) map.get("function");
        result.sleepTimeMillis = ((Number) ((Map) function.get("config")).get("sleep")).longValue() * 1000;

        return result;
    }
}
