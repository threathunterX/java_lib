package com.threathunter.variable.json;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.*;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.threathunter.common.Utility.getParentPackageName;

/**
 * created by www.threathunter.cn
 */
public class JsonUtil {
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        SimpleModule module = new SimpleModule(getParentPackageName(JsonUtil.class), new Version(1, 0, 0, null));
        module.addDeserializer(VariableMeta.class, new VariableMetaJsonDeserializer());
        module.addDeserializer(PropertyCondition.class, new PropertyConditionJsonDeserializer());
        module.addDeserializer(PropertyMapping.class, new PropertyMappingJsonDeserializer());
        module.addDeserializer(PropertyReduction.class, new PropertyReductionJsonDeserializer());
        module.addDeserializer(Identifier.class, new IdentifierDeserializer());
        module.addDeserializer(NamedType.class, new NamedTypeProcessor.NamedTypeDeserializer());

        module.addSerializer(Identifier.class, new IdentifierSerializer());
        module.addSerializer(NamedType.class, new NamedTypeProcessor.NamedTypeSerializer());
        mapper.registerModule(module);
        return mapper;
    }

    public static List<Object> property_list_to_json_object(List<Property> properties) {
        List<Object> result = new ArrayList<>();
        if (properties != null) {
            for(Property p : properties) {
                result.add(p.to_json_object());
            }
        }
        return result;
    }

    public static List<Property> property_list_from_json_object(List<Object> objects) {
        List<Property> result = new ArrayList<>();
        if (objects != null) {
            for(Object o : objects) {
                result.add(Property.from_json_object(o));
            }
        }
        return result;
    }

    public static List<Object> propertyreduction_list_to_json_object(List<PropertyReduction> reductions) {
        List<Object> result = new ArrayList<>();
        if (reductions != null) {
            for(PropertyReduction r : reductions) {
                result.add(r.to_json_object());
            }
        }
        return result;
    }

    public static List<PropertyReduction> propertyreduction_list_from_json_object(List<Object> objects) {
        List<PropertyReduction> result = new ArrayList<>();
        if (objects != null) {
            for(Object o : objects) {
                result.add(PropertyReduction.from_json_object(o));
            }
        }
        return result;
    }

    public static List<Object> propertymapping_list_to_json_object(List<PropertyMapping> mappings) {
        List<Object> result = new ArrayList<>();
        if (mappings != null) {
            for(PropertyMapping m : mappings) {
                result.add(m.to_json_object());
            }
        }
        return result;
    }

    public static List<PropertyMapping> propertymapping_list_from_json_object(List<Object> objects) {
        List<PropertyMapping> result = new ArrayList<>();
        if (objects != null) {
            for(Object o : objects) {
                result.add(PropertyMapping.from_json_object(o));
            }
        }
        return result;
    }

    public static List<Object> identifier_list_to_json_object(List<Identifier> ids) {
        List<Object> result = new ArrayList<>();
        if (ids != null) {
            for(Identifier id : ids) {
                result.add(id.to_json_object());
            }
        }
        return result;
    }

    public static List<Identifier> identifier_list_from_json_object(List<Object> objects) {
        List<Identifier> result = new ArrayList<>();
        if (objects != null) {
            for(Object o : objects) {
                result.add(Identifier.from_json_object(o));
            }
        }
        return result;
    }

    public static Object identifier_to_json_object(Identifier id) {
        if (id == null) {
            return null;
        }

        return id.to_json_object();
    }

    public static Identifier identifier_from_json_object(Object object) {
        if (object == null) {
            return null;
        }

        return Identifier.from_json_object(object);
    }

    public static Object property_to_json_object(Property property) {
        if (property == null) {
            return null;
        }

        return property.to_json_object();
    }

    public static Property property_from_json_object(Object object) {
        if (object == null) {
            return null;
        }

        return Property.from_json_object(object);
    }

    public static Object propertycondition_to_json_object(PropertyCondition condition) {
        if (condition == null) {
            return null;
        }

        return condition.to_json_object();
    }

    public static PropertyCondition propertycondition_from_json_object(Object object) {
        if (object == null || (object instanceof Map && ((Map) object).isEmpty())) {
            return null;
        }

        return PropertyCondition.from_json_object(object);
    }
}
