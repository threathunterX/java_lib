package com.threathunter.variable.mapping;

import com.threathunter.common.Identifier;
import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyMapping;
import com.threathunter.model.VariableMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Directly mapping the whole variable value to a new property in the new variable.
 *
 * <p>Usually, we only using properties of source variables, while in few cases we may need to store the whole source
 * variable(in the form of map) as a property in the variable.
 *
 * @author Wen Lu
 */
public class VariablePropertyMapping extends PropertyMapping {
    public static final String TYPE = "variable";
    static {
        PropertyMapping.addSubClass(TYPE, VariablePropertyMapping.class);
    }

    // for json
    protected VariablePropertyMapping() {}

    public VariablePropertyMapping(Identifier srcVariable, Property newProperty) {
        super(Arrays.asList(Property.buildDoubleProperty(srcVariable, "value"))/*value is a fixed property*/
                , newProperty.deepCopy(), TYPE);
    }

    public VariablePropertyMapping(VariableMeta srcVariable, Property newProperty) {
        this(Identifier.fromKeys(srcVariable.getApp(), srcVariable.getName()), newProperty);
    }

    @Override
    public Object to_json_object() {
        Map<Object, Object> result = new HashMap<>();
        result.put("type", getType());
        result.put("srcIdentifier", getSrcProperties().get(0).getIdentifier().to_json_object());
        result.put("destProperty", getDestProperty().to_json_object());
        return result;
    }

    public static VariablePropertyMapping from_json_object(Object obj) {
        Map<Object, Object> map = (Map<Object, Object>)obj;

        Identifier id = Identifier.from_json_object(map.get("srcIdentifier"));
        Property p = Property.from_json_object(map.get("destProperty"));
        p.setType(NamedType.MAP);
        return new VariablePropertyMapping(id, p);
    }
}
