package com.threathunter.variable;

import com.threathunter.model.BaseEventMeta;
import com.threathunter.model.EventMetaRegistry;
import com.threathunter.model.VariableMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by daisy on 17-11-23
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
        List<VariableMeta> metas = builder.buildFromJson(JsonFileReader.getValuesFromFile("test.json", JsonFileReader.ClassType.LIST));
        System.out.println(metas.size());
    }
}
