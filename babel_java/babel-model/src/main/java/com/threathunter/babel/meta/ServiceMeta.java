package com.threathunter.babel.meta;

import com.threathunter.model.EventMeta;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class ServiceMeta {
    private String name;
    private String deliverMode;
    private String callMode;
    private String serverImpl;
    private String coder;
    private Map<String, Object> options;

    public ServiceMeta() {
        options = new HashMap<>();
    }

    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("name", this.name);
        result.put("delivermode", this.deliverMode);
        result.put("callmode", this.callMode);
        result.put("serverimpl", this.serverImpl);
        result.put("coder", this.coder);
        result.put("options", this.options);
        return result;
    }

    public static ServiceMeta from_json_object(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> objMap = (Map<String, Object>) obj;
        ServiceMeta result = new ServiceMeta();
        result.name = (String) objMap.getOrDefault("name", "");
        result.deliverMode = (String) objMap.getOrDefault("delivermode", "");
        result.callMode = (String) objMap.getOrDefault("callmode", "");
        result.serverImpl = (String) objMap.getOrDefault("serverimpl", "");
        result.coder = (String) objMap.getOrDefault("coder", "");
        result.options = (Map<String, Object>) objMap.get("options");
        return result;
    }

    public static ServiceMeta from_json(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return from_json_object(mapper.reader(Map.class).readValue(json));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public EventMeta getRequestEventMeta() {
        return null;
    }

    public EventMeta getResponseEventMeta() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeliverMode() {
        return deliverMode;
    }

    public void setDeliverMode(String deliverMode) {
        this.deliverMode = deliverMode;
    }

    public String getCallMode() {
        return callMode;
    }

    public void setCallMode(String callMode) {
        this.callMode = callMode;
    }

    public String getServerImpl() {
        return serverImpl;
    }

    public void setServerImpl(String serverImpl) {
        this.serverImpl = serverImpl;
    }

    public String getCoder() {
        return coder;
    }

    public void setCoder(String coder) {
        this.coder = coder;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOption(String name, Object value) {
        this.options.put(name, value);
    }

    public Object getOption(String name) {
        return this.options.get(name);
    }

    public Object getOptionOrDefault(String name, Object defaultValue) {
        return this.options.getOrDefault(name, defaultValue);
    }
}
