package com.threathunter.basictools.babelmocker;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.meta.ServiceMetaUtil;
import com.threathunter.babel.rpc.RemoteException;
import com.threathunter.babel.rpc.Service;
import com.threathunter.babel.rpc.ServiceClient;
import com.threathunter.babel.rpc.ServiceContainer;
import com.threathunter.babel.rpc.impl.ServerContainerImpl;
import com.threathunter.babel.rpc.impl.ServiceClientImpl;
import com.threathunter.config.CommonDynamicConfig;
import com.threathunter.model.Event;
import com.threathunter.model.EventMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by daisy on 17-11-9
 */
public class RequestReceiveTest {

    @BeforeClass
    public static void setup() {
        CommonDynamicConfig.getInstance().addConfigFile("mock.conf");
    }

    @Test
    public void testReceiveHttplog() throws InterruptedException {
        ServiceContainer container = new ServerContainerImpl();
        ServiceMeta meta = ServiceMetaUtil.getMetaFromResourceFile("httplog_notify.service");
        AtomicInteger integer = new AtomicInteger(0);
        container.addService(new Service() {
            @Override
            public Event process(Event event) {
                System.out.println(event);
                integer.incrementAndGet();
                return null;
            }

            @Override
            public ServiceMeta getServiceMeta() {
                return meta;
            }

            @Override
            public EventMeta getRequestEventMeta() {
                return null;
            }

            @Override
            public EventMeta getResponseEventMeta() {
                return null;
            }

            @Override
            public void close() {

            }
        });
        container.start();

        while (integer.get() < 3) {
            Thread.sleep(100);
        }
        container.stop();
    }

    @Test
    public void testResponse() throws RemoteException {
        ServiceMeta meta = ServiceMetaUtil.getMetaFromResourceFile("variable_query.service");
        ServiceClient client = new ServiceClientImpl(meta);
        client.start();

        Event event = new Event("nebula", "variable_query", "");
        Map<String, Object> properties = new HashMap<>();
        properties.put("condition", "tag1");
        event.setPropertyValues(properties);
        System.out.print(client.rpc(event, meta.getName(), 1, TimeUnit.SECONDS));

        client.stop();
    }
}
