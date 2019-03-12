package com.threathunter.common;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 *
 * This service is used to generate the notifications in the background, so the users can get waked up.
 */
public class BackgroundNotifyService {

    private static final Map<Object, Runnable> listeners = new IdentityHashMap<>();
    private static final BackgroundNotifyService _instance = new BackgroundNotifyService();

    public static BackgroundNotifyService instance() {
        return _instance;
    }

    private BackgroundNotifyService() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            runListeners();
        }, 0, 60, TimeUnit.SECONDS);
        ShutdownHookManager.get().addShutdownHook(() -> runListeners(), 100);
    }

    public synchronized void addListener(Object obj, Runnable r) {
        this.listeners.put(obj, r);
    }

    public synchronized void removeListerner(Object obj) {
        this.listeners.remove(obj);
    }

    public synchronized void runListeners()  {
        for (Runnable r : listeners.values()) {
            try {
                r.run();
            } catch (Exception ignore) {
            }
        }
    }
}
