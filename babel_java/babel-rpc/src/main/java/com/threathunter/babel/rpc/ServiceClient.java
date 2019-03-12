package com.threathunter.babel.rpc;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.model.Event;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 */
public interface ServiceClient extends Closeable {

    void bindService(ServiceMeta meta);

    Event rpc(Event request, String destination, long timeout, TimeUnit unit) throws RemoteException;

    List<Event> polling(Event request, String destination, long timeout, TimeUnit unit) throws RemoteException;

    void notify(Event request, String destination, long timeout, TimeUnit unit) throws RemoteException;

    void notify(Event request, String destination) throws RemoteException;

    void notify(List<Event> requests, String destination, long timeout, TimeUnit unit) throws RemoteException;

    void notify(List<Event> requests, String destination) throws RemoteException;

//    void notify(List<Event> request, String destination, long sendWaitTime, TimeUnit unit, long sendWaitCount);

    void start();

    void stop();

    @Override
    void close() throws IOException;
}
