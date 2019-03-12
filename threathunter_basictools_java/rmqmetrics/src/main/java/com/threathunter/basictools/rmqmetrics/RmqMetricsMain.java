package com.threathunter.basictools.rmqmetrics;

import com.threathunter.config.CommonDynamicConfig;

/**
 * created by www.threathunter.cn
 */
public class RmqMetricsMain {
    public static boolean running = true;
    public static void main(String[] args) {
        CommonDynamicConfig.getInstance().addConfigFile("rmq_metrics.conf");

        final RmqMetricsWriter rmqMetricsWriter = new RmqMetricsWriter();
        rmqMetricsWriter.startWrite();
        System.out.println("started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("shutting down");
                rmqMetricsWriter.stopWrite();
                running = false;
            }
        });
        while (running) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                ;
            }
        }
    }
}
