package com.threathunter.variable.meta;

import com.threathunter.common.NamedType;
import com.threathunter.model.EventMetaRegistry;
import com.threathunter.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Describe the EventVariableMeta, and build the EventVariableMeta-specific information
 *
 * created by www.threathunter.cn
 */
public class EventVariableMeta extends BaseVariableMeta {
    public static final String TYPE = "event";
    static {
        addSubClass(TYPE, EventVariableMeta.class);
    }

    protected EventVariableMeta() {}

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    protected void parseFunction(Map<String, Object> functionJson) {

    }

    @Override
    protected List<Property> parseProperties() {
        return new ArrayList<>(EventMetaRegistry.getInstance().getEventMeta(this.srcEventMetaID).getProperties());
    }

    @Override
    protected NamedType parseValueType() {
        return NamedType.DOUBLE;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return super.toString() + "&&" + "EventVariableMeta{}";
    }

    public static EventVariableMeta from_json_object(Object obj) {
        EventVariableMeta result = new EventVariableMeta();
        result = from_json_object(obj, result);
        return result;
    }
}
