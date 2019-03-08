package com.threathunter.metrics.util;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.Closeable;

/**
 * Created by daisy on 2015/5/21.
 */

public abstract class Pool<T> implements Closeable {
    protected GenericObjectPool<T> internalPool;

    public Pool() {
    }

    public void close() {
        this.closeInternalPool();
    }

    public boolean isClosed() {
        return this.internalPool.isClosed();
    }

    public Pool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
        this.initPool(poolConfig, factory);
    }

    public void initPool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
        if(this.internalPool != null) {
            try {
                this.closeInternalPool();
            } catch (Exception e) {
                ;
            }
        }

        this.internalPool = new GenericObjectPool(factory, poolConfig);
    }

    public T getResource() {
        try {
            return this.internalPool.borrowObject();
        } catch (Exception var2) {
            throw new RuntimeException("Could not get a resource from the pool", var2);
        }
    }

    public void returnResourceObject(T resource) {
        if(resource != null) {
            try {
                this.internalPool.returnObject(resource);
            } catch (Exception var3) {
                throw new RuntimeException("Could not return the resource to the pool", var3);
            }
        }
    }

    public void returnBrokenResource(T resource) {
        if(resource != null) {
            this.returnBrokenResourceObject(resource);
        }

    }

    public void returnResource(T resource) {
        if(resource != null) {
            this.returnResourceObject(resource);
        }

    }

    public void destroy() {
        this.closeInternalPool();
    }

    protected void returnBrokenResourceObject(T resource) {
        try {
            this.internalPool.invalidateObject(resource);
        } catch (Exception var3) {
            throw new RuntimeException("Could not return the resource to the pool", var3);
        }
    }

    protected void closeInternalPool() {
        try {
            this.internalPool.close();
        } catch (Exception var2) {
            throw new RuntimeException("Could not destroy the pool", var2);
        }
    }

    public int getNumActive() {
        return this.internalPool != null && !this.internalPool.isClosed()?this.internalPool.getNumActive():-1;
    }

    public int getNumIdle() {
        return this.internalPool != null && !this.internalPool.isClosed()?this.internalPool.getNumIdle():-1;
    }

    public int getNumWaiters() {
        return this.internalPool != null && !this.internalPool.isClosed()?this.internalPool.getNumWaiters():-1;
    }
}
