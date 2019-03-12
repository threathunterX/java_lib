package com.threathunter.babel.rmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * created by www.threathunter.cn
 */
public class RmqBaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(RmqBaseConnection.class);
    private Connection connection;
    private Channel channel;

    public RmqBaseConnection() {
        connection = getConnection();
        channel = getChannel();
    }

    protected void destroyConnection() {
        logger.debug("destroy connection");
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                ;
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                ;
            }
        }
        channel = null;
        connection = null;
    }

    private Connection getConnection() {
        try {
            if (connection != null && connection.isOpen()) {
                return connection;
            } else {
                logger.debug(this.getClass().toString() + ": new connection");
                connection = RmqConnectionFactory.getNewConnection();
            }
        } catch (Exception e) {
            logger.error(this.getClass().toString() + ": failed to get new connection");
        }
        return connection;
    }

    protected Channel getChannel() {
        try {
            if ((channel != null && channel.isOpen())) {
                return channel;
            }
            channel = getConnection().createChannel();
        } catch (Exception e) {
            logger.error(this.getClass().toString() + ": failed to get new channel");
        }
        return channel;
    }

    protected void closeChannelAndConnection() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {
                logger.error(this.getClass().toString() + ": close connection error", e);
            }
        }
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.error(this.getClass().toString() + ": close connection error", e);
            }
        }
    }
}
