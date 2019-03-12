package com.threathunter.variable.meta;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;
import com.threathunter.model.VariableMeta;
import com.threathunter.model.VariableMetaRegistry;
import com.threathunter.variable.condition.ConditionParser;
import com.threathunter.variable.mapping.MappingParser;

import java.util.*;

import static com.threathunter.variable.json.JsonUtil.propertymapping_list_from_json_object;

/**
 * Describe the FilterVariableMeta, and build the FilterVariableMeta-specific information
 *
 * created by www.threathunter.cn
 */
public class FilterVariableMeta extends BaseVariableMeta {
    public static final String TYPE = "filter";
    static {
        addSubClass(TYPE, FilterVariableMeta.class);
    }

    FilterVariableMeta() {
    }

    @Override
    public String toString() {
        return super.toString() + "&&" + "FilterVariableMeta{" +
                "condition=" + condition +
                ", mappings=" + mappings +
                '}';
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = (Map<Object, Object>) super.to_json_object();

        result.put("filter_field", ConditionParser.toJsonObject(this.condition));
        Map<String, Object> functionMap = new HashMap<>();
        functionMap.put("mappings", generateMappingsToJson(this.mappings));
        result.put("function", functionMap);
        return result;
    }

    @Override
    protected void initialFunction(Map<String, Object> functionJson) {
        if (functionJson != null) {
            this.setMappings(propertymapping_list_from_json_object(
                    generateMappingsFromJson((List<Map<String, Object>>) functionJson.get("mappings"),
                            this.srcVariableMetasID.get(0), this.id)));
        } else {
            this.setMappings(propertymapping_list_from_json_object(
                    generateMappingsFromJson(null, this.srcVariableMetasID.get(0), this.id)));
        }
    }

    @Override
    protected List<Property> parseProperties() {
        List<Property> propertyList = new ArrayList<>();
        if (this.mappings == null || this.mappings.isEmpty()) {
            return propertyList;
        }
        this.mappings.forEach(mapping ->
            propertyList.add(mapping.getDestProperty()));

        return propertyList;
    }

    public static FilterVariableMeta from_json_object(Object obj) {
        FilterVariableMeta result = new FilterVariableMeta();
        result = from_json_object(obj, result);

        result.setValueType(NamedType.fromCode(VariableMetaRegistry.getInstance().getVariableMeta(result.getSrcVariableMetasID().get(0)).getValueType()));
        return result;
    }

    private static List<Object> generateMappingsToJson(List<PropertyMapping> currentMappings) {
        List<Object> target = new ArrayList<>();
        currentMappings.forEach(mapping -> {
            if (mapping.getType().equals("direct")) {
                if (mapping.getSrcProperties().get(0).getName() != mapping.getDestProperty().getName()) {
                    target.add(MappingParser.toJsonObject(mapping));
                }
            } else {
                target.add(MappingParser.toJsonObject(mapping));
            }
        });
        return target;
    }

    private static List<Object> generateMappingsFromJson(List<Map<String, Object>> originMaps, Identifier srcId, Identifier destId) {
        List<Object> mappings = new ArrayList<>();
        VariableMeta srcMeta = VariableMetaRegistry.getInstance().getVariableMeta(srcId);
        Set<String> destSet = new HashSet<>();
        if (originMaps != null) {
            originMaps.forEach(o -> {
                mappings.add(MappingParser.parseFrom(o, srcMeta, destId));
                destSet.add((String) o.get("dest"));
            });
        }

        srcMeta.getProperties().forEach(property -> {
            if (!destSet.contains(property.getName())) {
                mappings.add(MappingParser.genDirect(property.getName(), srcMeta, destId));
            }
        });

        return mappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FilterVariableMeta that = (FilterVariableMeta) o;

        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
        return mappings != null ? mappings.equals(that.mappings) : that.mappings == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (mappings != null ? mappings.hashCode() : 0);
        return result;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
