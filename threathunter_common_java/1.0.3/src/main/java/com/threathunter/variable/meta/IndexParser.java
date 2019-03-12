package com.threathunter.variable.meta;

import com.threathunter.common.Identifier;
import com.threathunter.model.Property;
import com.threathunter.model.VariableMeta;
import com.threathunter.model.VariableMetaRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * created by www.threathunter.cn
 */
public class IndexParser {
    public static Object parseFrom(String key, List<Identifier> srcIds) {
        for (Identifier id : srcIds) {
            VariableMeta meta = VariableMetaRegistry.getInstance().getVariableMeta(id);
            Property p = meta.findPropertyByName(key);
            if (p != null) {
               return p.to_json_object();
            }
        }

        return null;
    }

    public static List<Object> parseFrom(List<String> keys, List<Identifier> srcIds) {
        List<Object> objects = new ArrayList<>();
        keys.forEach(key -> {
            Object result = parseFrom(key, srcIds);
            if (result != null) {
                objects.add(result);
            }
        });

        return objects;
    }

    public static List<Object> parseFrom(List<String> keys, Identifier srcId) {
        List<Object> objects = new ArrayList<>();
        if (keys == null) {
            return objects;
        }
        VariableMeta meta = VariableMetaRegistry.getInstance().getVariableMeta(srcId);
        keys.forEach(key -> {
            Property p = meta.findPropertyByName(key);
            if (p != null) {
                objects.add(p.to_json_object());
            }
        });

        return objects;
    }

    public static List<String> toJsonObject(List<Property> indexes) {
        List<String> list = new ArrayList<>();
        if (indexes == null || indexes.isEmpty()) {
            return list;
        }
        indexes.forEach(index -> list.add(index.getName()));
        return list;
    }
}
