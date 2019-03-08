package com.threathunter.basictools.babelmocker;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.RemoteException;
import com.threathunter.babel.rpc.ServiceClient;
import com.threathunter.babel.rpc.impl.ServiceClientImpl;

import java.util.Map;

/**
 * Created by daisy on 17-11-8
 */
public class ServiceClientProxy {
    private final ServiceMeta meta;
    private final ServiceClient client;
    private final RequestResponseHolder requestHolder;

    public ServiceClientProxy(ServiceMeta meta, Map<String, Object> request) {
        this.meta = meta;
        this.requestHolder = new RequestResponseHolder(request);
        this.client = new ServiceClientImpl(meta);
    }

    public void start() {
        this.client.start();
    }

    public void stop() {
        this.client.stop();
    }

    public void sendRequest(String condition) throws RemoteException {
        this.client.notify(this.requestHolder.getEvent(condition), condition);
    }
}
