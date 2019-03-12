package com.threathunter.variable;

import com.threathunter.model.BaseEventMeta;
import com.threathunter.model.EventMetaRegistry;
import com.threathunter.model.VariableMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * created by www.threathunter.cn
 */
public class SlotVariableMetaTest {
    @BeforeClass
    public static void initial() throws IOException {
        List<Object> events = JsonFileReader.getValuesFromFile("events.json", JsonFileReader.ClassType.LIST);
        events.forEach(event -> EventMetaRegistry.getInstance().addEventMeta(BaseEventMeta.from_json_object(event)));
    }

    @Test
    public void testSlotMetaGraph() throws IOException {
        VariableMetaBuilder builder = new VariableMetaBuilder();
        List<VariableMeta> metas = builder.buildFromJson(JsonFileReader.getValuesFromFile("huazhu_dashboard_slot.json", JsonFileReader.ClassType.LIST));
        System.out.println(metas.size());
        System.out.println(metas.get(0).toString());
        System.out.println(metas.get(0).to_json_object());
    }
}
