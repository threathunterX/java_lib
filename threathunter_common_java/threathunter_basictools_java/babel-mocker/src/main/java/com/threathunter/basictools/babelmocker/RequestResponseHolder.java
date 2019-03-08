package com.threathunter.basictools.babelmocker;

import com.threathunter.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by daisy on 17-11-8
 */
public class RequestResponseHolder {
    private final String app;
    private final String name;
    private final String keyField;
    private final String type;

    private Random random;
    private List<Map<String, Object>> propertiesArray;
    private Map<String, Map<String, Object>> mapProperties;

    public RequestResponseHolder(Map<String, Object> data) {
        this.app = (String) data.getOrDefault("app", "");
        this.name = (String) data.getOrDefault("name", "");
        this.keyField = (String) data.getOrDefault("key_field", "");
        Object properties = data.get("properties");
        if (properties instanceof List) {
            this.random = new Random();
            this.type = "list";
            this.propertiesArray = new ArrayList<>((List)properties);
        } else {
            this.type = "map";
            this.mapProperties = (Map<String, Map<String, Object>>) properties;
        }
    }

    public Event getEvent(String condition) {
        String key = "";
        Map<String, Object> properties;
        if (type == "list") {
            properties = propertiesArray.get(random.nextInt(propertiesArray.size()));
        } else {
            properties = mapProperties.get(condition);

        }
        if (!this.keyField.isEmpty()) {
            key = (String) properties.getOrDefault(this.keyField, "");
        }
        return new Event(app, name, key, System.currentTimeMillis(), 1.0, properties);
    }
}
