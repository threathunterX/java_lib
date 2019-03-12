package com.threathunter.basictools.babelmocker;

import java.util.concurrent.ThreadFactory;

/**
 * created by www.threathunter.cn
 */
public enum DaemonThread implements ThreadFactory {
    INSTANCE;

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
