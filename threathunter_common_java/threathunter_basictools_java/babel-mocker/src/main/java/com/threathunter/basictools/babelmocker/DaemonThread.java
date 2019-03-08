package com.threathunter.basictools.babelmocker;

import java.util.concurrent.ThreadFactory;

/**
 * Created by daisy on 17-11-8
 */
public enum DaemonThread implements ThreadFactory {
    INSTANCE;

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
