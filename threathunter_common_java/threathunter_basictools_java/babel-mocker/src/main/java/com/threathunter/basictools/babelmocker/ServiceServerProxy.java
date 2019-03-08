package com.threathunter.basictools.babelmocker;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.Service;
import com.threathunter.babel.rpc.ServiceContainer;
import com.threathunter.babel.rpc.impl.ServerContainerImpl;
import com.threathunter.model.Event;
import com.threathunter.model.EventMeta;

import java.util.Map;

/**
 * Created by daisy on 17-11-8
 */
public class ServiceServerProxy {
    private final ServiceMeta meta;
    private final ServiceContainer container;
    private final RequestResponseHolder responseHolder;

    public ServiceServerProxy(ServiceMeta meta, Map<String, Object> response) {
        this.meta = meta;
        this.responseHolder = new RequestResponseHolder(response);
        this.container = new ServerContainerImpl();
        this.container.addService(new Service() {
            @Override
            public Event process(Event event) {
                String condition = (String) event.getPropertyValues().getOrDefault("condition", "");
                return responseHolder.getEvent(condition);
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
    }

    public void start() {
        this.container.start();
    }

    public void stop() {
        this.container.stop();
    }
}
