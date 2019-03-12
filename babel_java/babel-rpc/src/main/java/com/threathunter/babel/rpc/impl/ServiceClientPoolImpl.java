package com.threathunter.babel.rpc.impl;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.ServiceClient;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * created by www.threathunter.cn
 */
public class ServiceClientPoolImpl {

    private AtomicLong ID = new AtomicLong();

    private String clientName;

    private ServiceMeta smeta;

    private Queue<ServiceClientImpl> freeList;

    public ServiceClientPoolImpl(ServiceMeta meta, String clientName,
                                 int maxFree) {
        this.smeta = meta;
        this.freeList = new ArrayBlockingQueue<>(maxFree);
        this.clientName = clientName;
    }

    public ServiceClientImpl getClient() {
        ServiceClientImpl result = freeList.poll();
        if (result != null) {
            return result;
        }

        try {
            result = newClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public void returnClient(ServiceClientImpl client) {
        if (!freeList.offer(client)) {
            // freelist is full
            client.stop();
        }
    }

    public void stop() {
        for (ServiceClient client : freeList) {
            client.stop();
        }
    }

    private ServiceClientImpl newClient() throws IOException {
        ServiceClientImpl client = new ServiceClientImpl(
                this.smeta, String.format("%s.%d", this.clientName, this.ID.incrementAndGet()));
        client.start();
        return client;
    }
}
