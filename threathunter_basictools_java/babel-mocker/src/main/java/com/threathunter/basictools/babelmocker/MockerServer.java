package com.threathunter.basictools.babelmocker;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.RemoteException;
import com.threathunter.config.CommonDynamicConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 */
public class MockerServer {
    private static Logger LOGGER = LoggerFactory.getLogger(MockerServer.class);
    private static final MockerServer INSTANCE = new MockerServer();

    private final Thread updateThread;
    private final ThreadPoolExecutor executor;

    private final ConcurrentHashMap<String, ServiceServerProxy> serverMap;
    private final ConcurrentHashMap<String, ServiceClientProxy> clientMap;
    private volatile boolean running = false;
    private volatile Set<String> inService;

    private MockerServer() {
        this.executor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(1000), DaemonThread.INSTANCE);
        this.updateThread = DaemonThread.INSTANCE.newThread(() -> {
            // in seconds
            int updateInterval = CommonDynamicConfig.getInstance().getInt("update_interval", 60);
            while (running) {
                update();
                try {
                    Thread.sleep(updateInterval * 1000);
                } catch (Exception e) {
                    LOGGER.error("[mock server] interrupt error", e);
                }
            }
        });

        this.serverMap = new ConcurrentHashMap<>();
        this.clientMap = new ConcurrentHashMap<>();
    }

    public static MockerServer getInstance() {
        return INSTANCE;
    }

    // start
    public void start() {
        if (this.running) {
            LOGGER.warn("[mock server] already started");
            return;
        }
        this.running = true;
        this.updateThread.start();
    }

    public void stop() {
        if (!this.running) {
            LOGGER.warn("[mock server] already stopped");
            return;
        }
        this.running = false;
        try {
            this.updateThread.join();
        } catch (Exception e) {
            LOGGER.error("[mock server] join interrupted", e);
        }
    }

    public void invokeClient(String serviceName, String condition) throws RemoteException {
        ServiceClientProxy proxy = this.clientMap.get(serviceName);
        if (proxy == null) {
            throw new RuntimeException("service client config is not exist, service: " + serviceName);
        }
        proxy.sendRequest(condition);
    }

    private void update() {
        String metaDir = CommonDynamicConfig.getInstance().getString("babel_meta_dir", System.getProperty("user.dir") + "/meta");
        String requestDir = CommonDynamicConfig.getInstance().getString("babel_request_dir", System.getProperty("user.dir") + "/request");
        String responseDir = CommonDynamicConfig.getInstance().getString("babel_response_dir", System.getProperty("user.dir") + "/response");
        ObjectMapper mapper = new ObjectMapper();

        Set<String> currentServices = new HashSet<>();
        File file = new File(metaDir);
        if (!file.exists()) {
            return;
        }
        for (File f : file.listFiles()) {
            try {
                String name = f.getName();
                ServiceMeta meta = ServiceMeta.from_json_object(mapper.readValue(new FileInputStream(f), Map.class));

                File requestFile = new File(String.format("%s/%s", requestDir, name));
                File responseFile = new File(String.format("%s/%s", responseDir, name));

                if (requestFile.exists()) {
                    Map<String, Object> request = mapper.readValue(new FileInputStream(requestFile), Map.class);
                    ServiceClientProxy newProxy = new ServiceClientProxy(meta, request);
                    ServiceClientProxy older = this.clientMap.put(name, newProxy);
                    if (older != null) {
                        older.stop();
                    }
                    newProxy.start();
                    currentServices.add(name);
                }
                if (responseFile.exists()) {
                    Map<String, Object> response = mapper.readValue(new FileInputStream(responseFile), Map.class);
                    ServiceServerProxy newProxy = new ServiceServerProxy(meta, response);
                    ServiceServerProxy older = this.serverMap.put(name, newProxy);
                    if (older != null) {
                        older.stop();
                    }
                    newProxy.start();
                    currentServices.add(name);
                }

            } catch (IOException e) {
                LOGGER.error("[mock server] read file error", e);
            }
        }

        if (this.inService != null) {
            this.inService.forEach(s -> {
                if (!currentServices.contains(s)) {
                    ServiceClientProxy cproxy = this.clientMap.remove(s);
                    if (cproxy != null) {
                        cproxy.stop();
                    }

                    ServiceServerProxy sproxy = this.serverMap.remove(s);
                    if (sproxy != null) {
                        sproxy.stop();
                    }
                }
            });
        }
        this.inService = currentServices;
    }
}
