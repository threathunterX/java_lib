package com.threathunter.variable.meta;

import com.threathunter.common.NamedType;
import com.threathunter.model.Property;
import com.threathunter.model.PropertyReduction;
import com.threathunter.model.VariableMeta;
import com.threathunter.model.VariableMetaRegistry;
import com.threathunter.variable.reduction.ReductionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 17-10-31
 */
public class TopVariableMeta extends BaseVariableMeta {
    public static final String TYPE = "top";
    static {
        addSubClass(TYPE, TopVariableMeta.class);
    }

    private String targetPropertyName;
    private String groupPropertyName;

    @Override
    protected void parseFunction(Map<String, Object> functionJson) {
        this.targetPropertyName = (String) functionJson.get("object");
        this.groupPropertyName = (String) functionJson.get("param");
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public String getGroupPropertyName() {
        return groupPropertyName;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected List<Property> parseProperties() {
        List<Property> propertyList = new ArrayList<>();
        if (this.groupKeys != null && groupKeys.size() > 0) {
            this.getGroupKeys().forEach(property -> propertyList.add(new Property(this.getId(), property.getName(), property.getType(), property.getSubType())));
        }
        return propertyList;
    }

    @Override
    protected NamedType parseValueType() {
        return NamedType.LIST;
    }

    public static TopVariableMeta from_json_object(final Object obj) {
        TopVariableMeta result = new TopVariableMeta();
        result = BaseVariableMeta.from_json_object(obj, result);

        result.setValueType(NamedType.fromCode(VariableMetaRegistry.getInstance().getVariableMeta(
                result.getSrcVariableMetasID().get(0)).getValueType()));
        return result;
    }
}
