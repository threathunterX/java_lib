package com.threathunter.babel.rpc.impl;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.Service;
import com.threathunter.babel.rpc.ServiceContainer;
import com.threathunter.babel.util.LocalServiceRegistry;
import com.threathunter.babel.util.ServiceConstant;
import com.threathunter.common.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * created by www.threathunter.cn
 */
public class ServerContainerImpl implements ServiceContainer {
    private static final Logger logger = LoggerFactory.getLogger(ServerContainerImpl.class);

    protected List<ServiceServer> serviceServiceServers = new ArrayList<>();

    @Override
    public void start() {
        for (ServiceServer serviceServer : this.serviceServiceServers) {
            try {
                serviceServer.startWork();
            } catch (Exception e) {
                logger.error("rpc:fatal:start serviceServer work error, serviceServer: " + serviceServer.getName(), e);
            }
        }
    }

    @Override
    public void stop() {
        for (ServiceServer serviceServer : this.serviceServiceServers) {
            try {
                serviceServer.stopWork();
            } catch (Exception e) {
                logger.error("close:rpc:stop serviceServer work error, serviceServer: " + serviceServer.getName(), e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.stop();
    }

    @Override
    public void addService(Service service) {
        Utility.argumentNotEmpty(service, "service is null");

        ServiceMeta serviceMeta = service.getServiceMeta();
        Utility.argumentNotEmpty(serviceMeta.getName(), "service request event name is null");

        try {
            addServiceServer(service);
        } catch (Exception e) {
            logger.error("rpc:create service server error", serviceMeta.to_json_object());
        }
    }

    @Override
    public void removeService(Service service) {
    }

    private void addServiceServer(Service service) {
        ServiceMeta meta = service.getServiceMeta();
        LocalServiceRegistry.getInstance().addServiceMeta(meta);
        String id = meta.getName();
        // subname is for TOPIC
        if (meta.getOptions().containsKey(ServiceConstant.OPTION_SUBNAME)) {
            id += "." + meta.getOptions().get(ServiceConstant.OPTION_SUBNAME);
        }
        ServiceServer serviceServer = null;
        try {
            serviceServer = new ServiceServer(id, service);
        } catch (Exception e) {
            logger.error("rpc:add service for event: {}", e);
        }
        serviceServer.putService(service);

        serviceServiceServers.add(serviceServer);

        logger.debug("add service for event: {}, serviceServer: {}", meta.getName(), serviceServer.getId());
    }
}
