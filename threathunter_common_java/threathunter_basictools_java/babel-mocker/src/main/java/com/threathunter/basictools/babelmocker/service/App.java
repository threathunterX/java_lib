package com.threathunter.basictools.babelmocker.service;

import com.threathunter.basictools.babelmocker.MockerServer;
import com.threathunter.config.CommonDynamicConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daisy on 17-11-8
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        CommonDynamicConfig.getInstance().addConfigFile("mock.conf");

        ResourceConfig config = new ResourceConfig();
        config.packages("com.threathunter.basictools.babelmocker.service");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));


        Server server = new Server(CommonDynamicConfig.getInstance().getInt("service_port", 2222));
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");

        try {
            MockerServer.getInstance().start();

            server.start();
            server.join();

            MockerServer.getInstance().stop();
        } catch (Exception e){
            LOGGER.error("[rest service] server error", e);
        } finally {
            server.destroy();
        }
    }
}
