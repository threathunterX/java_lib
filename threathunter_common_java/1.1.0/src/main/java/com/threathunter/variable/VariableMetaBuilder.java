package com.threathunter.variable;

import com.threathunter.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by daisy on 17/8/24.
 */
public class VariableMetaBuilder {
    static {
        PropertyCondition.init();
        PropertyMapping.init();
        PropertyReduction.init();
        VariableMeta.init();
    }

    private VariableGraphMetaInitializer initializer = new VariableGraphMetaInitializer();

    /**
     * This is for build all the metas once, the return metas will be sorted by priority
     * Also at the same time, the meta will be registered in VariableMetaRegistry
     * @param listObj
     * @return
     */
    public List<VariableMeta> buildFromJson(List<Object> listObj) {
        // initial the metas, mainly for explicit the priority, and sorted by priority
        List<Map<String, Object>> initialedMetaJson = initializer.initialGraphMetaObject(listObj);

        List<VariableMeta> metas = new ArrayList<>();
        // build meta one by one, because the initialed is sorted by priority
        // will also need to parse for the mappings, conditions, and reductions first
        initialedMetaJson.forEach(metaJson -> {
            // handled by every type of meta-builder, the only difference is the filter_field and function
            VariableMeta meta = VariableMeta.from_json_object(metaJson);
            metas.add(meta);
            VariableMetaRegistry.getInstance().addVariableMeta(meta);
        });
        return metas;
    }

    public Object toJsonObject(VariableMeta meta) {
        return meta.to_json_object();
    }

    public List<Object> toJsonObject(List<VariableMeta> metas) {
        List<Object> result = new ArrayList<>();
        metas.forEach(meta -> result.add(meta.to_json_object()));
        return result;
    }
}
