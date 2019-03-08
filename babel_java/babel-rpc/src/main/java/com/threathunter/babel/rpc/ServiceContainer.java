package com.threathunter.babel.rpc;

import java.io.Closeable;
import java.io.IOException;

/**
 * An ServiceContainer is the container of the service providers.
 *
 * @author Wen Lu
 */
public interface ServiceContainer extends Closeable {
    void addService(Service service);

    void removeService(Service service);
    /**
     * Start the redis server.
     *
     */
    void start();

    /**
     * Stop the server
     */
    void stop();

    /**
     * stop the server.
     *
     * @throws IOException
     */
    @Override
    void close() throws IOException;
}
