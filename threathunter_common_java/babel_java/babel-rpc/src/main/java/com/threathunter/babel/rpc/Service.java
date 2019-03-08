package com.threathunter.babel.rpc;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.model.Event;
import com.threathunter.model.EventMeta;

/**
 * RPC service is a service that can be called remotely.
 *
 * This service is different from traditional rpc services. it uses event as the carrier for parameters and results.
 * This service is used in the sysytem for information exchanging.
 *
 * @author Wen Lu
 */
public interface Service {

    /**
     * The processing logic of the service.
     *
     * @param e the incoming event as parameter of the service
     * @return The result event of the service, or null if there is not a result for this service
     */
    Event process(Event e);

    ServiceMeta getServiceMeta();

    /**
     * @return the request event meta
     */
    EventMeta getRequestEventMeta();

    /**
     * @return the response event meta, null if it's a one way service
     */
    EventMeta getResponseEventMeta();

    /**
     * Close some resources if needs
     */
    void close();
}