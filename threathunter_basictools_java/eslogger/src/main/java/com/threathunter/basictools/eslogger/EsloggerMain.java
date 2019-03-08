package com.threathunter.basictools.eslogger;

import com.threathunter.metrics.MetricsAgent;

/**
 * Created by daisy on 2015/9/18.
 */
public class EsloggerMain {
    private static volatile boolean running = false;

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Missing config folder path");
        }
        String configFolder = args[0];
        String metricsConfig = configFolder + "/metrics.yml";
        String serviceMetaFile = configFolder + "/eslogger.json";
        String esConfig = configFolder + "/es.yml";

        MetricsAgent.getInstance().start(metricsConfig);

        final EsloggerServer esloggerServer = new EsloggerServer(esConfig, serviceMetaFile);
        esloggerServer.startServer();
        System.out.println("log service started");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;
                esloggerServer.stopServer();
                MetricsAgent.getInstance().stop();
                System.out.println("Service stopped");
            }
        });

        while (running) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }
}
