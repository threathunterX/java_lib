package com.threathunter.babel.rmq;

import com.threathunter.config.CommonDynamicConfig;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * created by www.threathunter.cn
 */
public class RmqConnectionFactory {
    private static ConnectionFactory factory;

    static {
        if (CommonDynamicConfig.getInstance().getString("babel_server") != null) {
            String rmq_host = CommonDynamicConfig.getInstance().getString("rmq_host", "127.0.0.1");
            int port = CommonDynamicConfig.getInstance().getInt("rmq_port", 5672);
            String username = CommonDynamicConfig.getInstance().getString("rmq_username");
            String password = CommonDynamicConfig.getInstance().getString("rmq_password");

            factory = new ConnectionFactory();
            factory.setHost(rmq_host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setConnectionTimeout(5000);
        } else {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("babel");

            factory = new ConnectionFactory();
            factory.setHost(resourceBundle.getString("rmq.cluster_ip"));
            factory.setPort(Integer.parseInt(resourceBundle.getString("rmq.cluster_port")));
            factory.setUsername(resourceBundle.getString("rmq.user"));
            factory.setPassword(resourceBundle.getString("rmq.password"));
            factory.setConnectionTimeout(5000);
        }
    }

    public static Connection getNewConnection() throws Exception {
        return factory.newConnection();
    }

    public static void setHost(String host) {
        factory.setHost(host);
    }

    public static void setPort(int port) {
        factory.setPort(port);
    }

    public static void setUsername(String username) {
        factory.setUsername(username);
    }

    public static void setPassword(String password) {
        factory.setPassword(password);
    }
}
