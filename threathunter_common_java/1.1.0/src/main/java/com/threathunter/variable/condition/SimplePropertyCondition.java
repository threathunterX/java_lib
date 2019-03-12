package com.threathunter.variable.condition;

import com.threathunter.model.Property;
import com.threathunter.model.PropertyCondition;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for the condition that is based on single property.
 *
 * created by www.threathunter.cn
 */
public abstract class SimplePropertyCondition extends PropertyCondition {
    private final Property property;
    private final Object param;

    // for json
    protected SimplePropertyCondition() {
        property = null;
        param = null;
    }

    public SimplePropertyCondition(Property property, String type, Object param) {
        super(type);
        this.property = property;
        this.param = param;
    }

    @Override
    public List<Property> getSrcProperties() {
        return Arrays.asList(property);
    }
}
