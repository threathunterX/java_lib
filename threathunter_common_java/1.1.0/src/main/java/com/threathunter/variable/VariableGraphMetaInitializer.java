package com.threathunter.variable;

import com.threathunter.common.Identifier;

import java.util.*;

/**
 * Created by daisy on 17-9-1
 */
public class VariableGraphMetaInitializer {

    /**
     * Supply the meta information, especially priority
     * Convert the implicit info to explicit node meta
     * Also will missing some contents like: condition, mapping and reduction
     * @param customerVariables
     * @return
     */
    public List<Map<String, Object>> initialGraphMetaObject(List<Object> customerVariables) {
        Map<Identifier, Map<String, Object>> graphMap = new HashMap<>();
        customerVariables.forEach(customer -> {
            try {
                Map<String, Object> implicit = (Map<String, Object>) customer;

                Map<String, Object> explicit = new HashMap<>();
                Identifier identifier = Identifier.fromKeys((String) implicit.get("app"), (String) implicit.get("name"));
                explicit.put("identifier", identifier);
                explicit.put("app", implicit.get("app"));
                explicit.put("name", implicit.get("name"));

                explicit.put("module", implicit.get("module"));
                explicit.put("remark", implicit.get("remark"));
                explicit.put("visible_name", implicit.get("visible_name"));
                explicit.put("dimension", implicit.get("dimension"));
                explicit.put("status", implicit.get("status"));
                explicit.put("groupkeys", implicit.get("groupbykeys"));
                explicit.put("type", implicit.get("type"));
                explicit.put("value_type", implicit.get("value_type"));

                List<Identifier> srcIdentifiers = new ArrayList<>();
                ((List<Map<String, String>>) implicit.get("source")).forEach(source ->
                        srcIdentifiers.add(Identifier.fromKeys(source.get("app"), source.get("name"))));
                explicit.put("parents", srcIdentifiers);

                // save conditions
                explicit.put("condition", implicit.get("filter"));

                // save function
                Map<String, Object> function = (Map<String, Object>) implicit.get("function");
                explicit.put("function", function);

                // save mappings
                if (function != null && !function.isEmpty()) {
                    explicit.put("mappings", function.getOrDefault("mappings", new ArrayList()));
                }

                Map<String, Object> ttlMap = (Map<String, Object>) implicit.get("period");
                if (ttlMap != null && !ttlMap.isEmpty()) {
                    explicit.put("period", getPeriodInSeconds(ttlMap));
                }

                graphMap.put(identifier, explicit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        addPriority(graphMap);

        List<Map<String, Object>> result = new ArrayList<>();
        result.addAll(graphMap.values());
        result.sort(Comparator.comparingInt(v -> (Integer) v.get("priority")));
        return result;
    }

    private void addPriority(Map<Identifier, Map<String, Object>> metas) {
        metas.values().forEach(meta -> {
            if (!meta.containsKey("priority")) {
                addPriority(meta, metas);
            }
        });
    }

    private void addPriority(Map<String, Object> target, Map<Identifier, Map<String, Object>> metas) {
        Integer max = 0;
        List<Identifier> parents = (List<Identifier>) target.get("parents");
        if (target.get("type").equals("event") || target.get("type").equals("internal")) {
            target.put("priority", max);
            return;
        }

        for (Identifier id : parents) {
            Integer p = (Integer) metas.get(id).get("priority");

            if (p == null) {
                addPriority(metas.get(id), metas);
            }
            p = (Integer) metas.get(id).get("priority");
            max = p > max ? p : max;
        }
        target.put("priority", max + 1);
        parents.forEach(id -> ((List) metas.get(id).computeIfAbsent("children", k -> new ArrayList<>()))
                .add(target.get("identifier")));

        if (target.get("type").equals("dual") && target.get("module").equals("realtime")) {
            String operator = (String) ((Map<String, Object>) target.get("function")).get("method");
            if (operator.equals("/") || operator.equals("-")) {

                // first parents of dual should be at least second + 1
                Identifier second = parents.get(1);
                Identifier first = parents.get(0);
                if (second == null || first == null) {
                    throw new RuntimeException("priority error, miss source for dual, target: " + target.get("name"));
                }
                Integer secondPriority = (Integer) metas.get(second).get("priority");
                Integer firstPriority = (Integer) metas.get(first).get("priority");
                if (firstPriority <= secondPriority) {
                    // shift the first priority and all the children's priority
                    shiftPriority(metas.get(first), secondPriority + 1 - firstPriority, metas);
                }
            }
        }
    }

    private void shiftPriority(Map<String, Object> target, Integer shiftPriority, Map<Identifier, Map<String, Object>> metas) {
        target.put("priority", (Integer) target.get("priority") + shiftPriority);

        List<Identifier> children = (List<Identifier>) target.get("children");
        if (children != null) {
            children.forEach(id -> {
                shiftPriority(metas.get(id), shiftPriority, metas);
            });
        }
    }

    private int getPeriodInSeconds(Map<String, Object> ttlMap) {
        String unit = (String) ttlMap.get("type");
        Object value = ttlMap.get("value");
        if (unit.equals("hourly")) {
            if (value instanceof Number) {
                return ((Number) value).intValue() * 3600;
            }
            return Integer.valueOf((String) ttlMap.get("value")) * 3600;
        }
        if (unit.equals("last_n_seconds")) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.valueOf((String) ttlMap.get("value"));
        }
        return 300;
    }
}
