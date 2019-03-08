package com.threathunter.variable.meta;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;
import com.threathunter.model.VariableMeta;
import com.threathunter.model.VariableMetaRegistry;
import com.threathunter.variable.condition.ConditionParser;
import com.threathunter.variable.mapping.DirectPropertyMapping;
import com.threathunter.variable.mapping.VariablePropertyMapping;

import java.util.*;

/**
 * Created by daisy on 17-9-5
 */
public class CollectorVariableMeta extends BaseVariableMeta {
    public static final String TYPE = "collector";
    static {
        addSubClass(TYPE, CollectorVariableMeta.class);
    }

    private Identifier trigger;
    private boolean onlyValueNeeded = true;
    private String strategyName;
    private String action;

    public Identifier getTrigger() {
        return trigger;
    }

    public void setTrigger(Identifier trigger) {
        this.trigger = trigger;
    }

    public boolean isOnlyValueNeeded() {
        return onlyValueNeeded;
    }

    public void setOnlyValueNeeded(boolean onlyValueNeeded) {
        this.onlyValueNeeded = onlyValueNeeded;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public Object to_json_object() {
        Map<String, Object> result = (Map<String, Object>) super.to_json_object();

        result.put("filter_field", ConditionParser.toJsonObject(this.condition));
        Map<String, Object> functionMap = new HashMap<>();
        functionMap.put("check", this.strategyName);
        result.put("function", functionMap);
        return result;
    }

    @Override
    protected void initialFunction(Map<String, Object> functionJson) {
        List<PropertyMapping> mappings = new ArrayList<>();
        this.srcVariableMetasID.forEach(srcId -> {
            VariableMeta meta = VariableMetaRegistry.getInstance().getVariableMeta(srcId);
            if (onlyValueNeeded) {
                mappings.add(new DirectPropertyMapping(meta.findPropertyByName("value"), this.id, meta.getName()));
            } else {
                mappings.add(new VariablePropertyMapping(meta, new Property(this.id, meta.getName(), NamedType.MAP)));
            }
        });
        this.setMappings(mappings);
    }

    @Override
    protected List<Property> parseProperties() {
        List<Property> propertyList = new ArrayList<>();
        this.mappings.forEach(mapping -> {
            Property destProperty = mapping.getDestProperty();
            propertyList.add(new Property(id, destProperty.getName(), destProperty.getType()));
        });
        return propertyList;
    }

    public static CollectorVariableMeta from_json_object(Object obj) {
        CollectorVariableMeta result = new CollectorVariableMeta();
        result = BaseVariableMeta.from_json_object(obj, result);
        Map<String, Object> map = (Map<String, Object>) obj;

        Map<String, Object> function = (Map<String, Object>) map.get("function");
        Map<String, Object> config = (Map<String, Object>) function.get("config");
        Identifier triggerId = Identifier.fromKeys(result.getApp(), (String) config.get("trigger"));
        result.setTrigger(triggerId);
        result.setOnlyValueNeeded((Boolean) config.getOrDefault("value_only", true));
        result.setStrategyName((String) function.get("param"));
        result.setAction((String) function.get("method"));

        return result;
    }

    public static <T extends CollectorVariableMeta> T from_json_object(Object obj, T result) {
        result = BaseVariableMeta.from_json_object(obj, result);
        Map<String, Object> map = (Map<String, Object>) obj;

        Map<String, Object> function = (Map<String, Object>) map.get("function");
        Map<String, Object> config = (Map<String, Object>) function.get("config");
        Identifier triggerId = Identifier.fromKeys(result.getApp(), (String) config.get("trigger"));
        result.setTrigger(triggerId);
        result.setOnlyValueNeeded((Boolean) config.getOrDefault("value_only", true));
        result.setStrategyName((String) function.get("param"));
        result.setAction((String) function.get("method"));

        return result;
    }
}
