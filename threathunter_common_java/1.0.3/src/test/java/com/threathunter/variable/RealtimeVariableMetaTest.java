package com.threathunter.variable;

import com.threathunter.model.BaseEventMeta;
import com.threathunter.model.EventMetaRegistry;
import com.threathunter.model.VariableMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by daisy on 17-11-24
 */
public class RealtimeVariableMetaTest {
    @BeforeClass
    public static void initial() throws IOException {
        List<Object> events = JsonFileReader.getValuesFromFile("events.json", JsonFileReader.ClassType.LIST);
        events.forEach(event -> EventMetaRegistry.getInstance().addEventMeta(BaseEventMeta.from_json_object(event)));
    }

    @Test
    public void testRealtimeMetaGraph() throws IOException {
        VariableMetaBuilder builder = new VariableMetaBuilder();
        List<VariableMeta> metas = builder.buildFromJson(JsonFileReader.getValuesFromFile("realtime_variable_default.json", JsonFileReader.ClassType.LIST));
        for (VariableMeta meta : metas) {
            if (meta.getName().equals("did__account_login_count__5m__rt")) {
                System.out.println(meta.getTtl());
            }
        }
        System.out.println(metas.size());
    }
}
