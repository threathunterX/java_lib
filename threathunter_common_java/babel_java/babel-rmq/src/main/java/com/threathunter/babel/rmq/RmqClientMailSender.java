package com.threathunter.babel.rmq;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailEncoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailSender;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.util.DeliverMode;
import com.threathunter.common.Utility;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static com.threathunter.babel.rmq.RmqConstant.*;

/**
 * Used to send request to server.
 *
 * Created by daisy on 2015/6/19.
 */
public class RmqClientMailSender extends RmqBaseConnection implements MailSender {

    private static final Logger logger = LoggerFactory.getLogger(RmqClientMailSender.class);

    private final MailEncoder coder;
    private final ServiceMeta meta;

    public RmqClientMailSender(ServiceMeta meta, MailEncoder coder) throws IOException {
        Utility.argumentNotEmpty(coder, "coder is null");
        Utility.argumentNotEmpty(meta, "service meta is null");

        this.coder = coder;
        this.meta = meta;

        setupExchange();
    }

    @Override
    public void sendMail(Mail m) throws MailException {
        if (m == null)
            return;

        try {
            DeliverMode mode = DeliverMode.fromString(this.meta.getDeliverMode());

            String exchange;
            String routing = meta.getName();

            String[] datacenters = null;
            if (m.getHeader("sdc") == null) {
                throw new RuntimeException("rmq:client:send:missing sdc in header");
            }
            datacenters = m.getHeader("sdc").split(",");

            if (mode == DeliverMode.SHARDING) {
                routing = m.getHeader("key");
                exchange = "_sharding." + meta.getName();
            } else if (mode == DeliverMode.TOPICSHARDING) {
                routing = m.getHeader("key");
                exchange = "_toshard." + meta.getName();
            } else if (mode == DeliverMode.QUEUE) {
                exchange = "_queue";
            } else if (mode == DeliverMode.TOPIC) {
                exchange = "_topic";
            } else if (mode == DeliverMode.SHUFFLE) {
                exchange = "_shuffle";
            } else if (mode == DeliverMode.TOPICSHUFFLE) {
                exchange = "_toffle";
            } else {
                throw new RuntimeException("unsupported deliver mode");
            }
            if ((Boolean) meta.getOptions().getOrDefault("durable", false)) {
                if (datacenters != null) {
                    for (String datacenter : datacenters) {
                        getChannel().basicPublish(String.format("%s.%s", datacenter, exchange), routing, new Builder().deliveryMode(2).build(), coder.encode(m).getBytes());
                    }
                } else {
                    getChannel().basicPublish(exchange, routing, new Builder().deliveryMode(2).build(), coder.encode(m).getBytes());
                }
            } else {
                if (datacenters != null) {
                    for (String datacenter : datacenters) {
                        getChannel().basicPublish(String.format("%s.%s", datacenter, exchange), routing, null, coder.encode(m).getBytes());
                    }
                } else {
                    getChannel().basicPublish(exchange, routing, null, coder.encode(m).getBytes());
                }
            }
        } catch (Exception e) {
            if (e.getClass().equals(ConnectException.class) || e.getClass().equals(SocketTimeoutException.class)) {
                destroyConnection();
            }
            logger.error(String.format("rabbitmq:rpc:rmq mail sender: publish error, to: %s", meta.getName()), e);

            throw new MailException(e);
        }
    }

    @Override
    public void closeConnection() {
        closeChannelAndConnection();
    }

    private void setupExchange() throws IOException {
        DeliverMode mode = DeliverMode.fromString(meta.getDeliverMode());
        Object clientDc = meta.getOptions().get("sdc");
        if (clientDc == null) {
            throw new RuntimeException("rmp:server:missing client datacenter in option.");
        }
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }
        if (mode == DeliverMode.SHARDING) {
            String exchange = String.format("%s.%s.%s", clientDc, RMQ_EXCHANGE_NAME_SHARDING, meta.getName());
            getChannel().exchangeDeclare(exchange, "x-consistent-hash", durable, autoDelete, null);
        } else if (mode == DeliverMode.TOPICSHARDING) {
            String exchange = String.format("%s.%s.%s", clientDc, RMQ_EXCHANGE_NAME_TOSHARD, meta.getName());
            getChannel().exchangeDeclare(exchange, "fanout", durable, autoDelete, null);
        } else if (mode == DeliverMode.QUEUE) {
            String exchange = String.format("%s.%s", clientDc, RMQ_EXCHANGE_NAME_QUEUE);
            getChannel().exchangeDeclare(exchange, "direct", true, false, null);
        } else if (mode == DeliverMode.TOPIC) {
            String exchange = String.format("%s.%s", clientDc, RMQ_EXCHANGE_NAME_TOPIC);
            getChannel().exchangeDeclare(exchange, "topic", true, false, null);
        } else if (mode == DeliverMode.SHUFFLE) {
            String exchange = String.format("%s.%s", clientDc, RMQ_EXCHANGE_NAME_SHUFFLE);
            getChannel().exchangeDeclare(exchange, "direct", true, false, null);
        } else if (mode == DeliverMode.TOPICSHUFFLE) {
            String exchange = String.format("%s.%s", clientDc, RMQ_EXCHANGE_NAME_TOFFLE);
            getChannel().exchangeDeclare(exchange, "topic", true, false, null);
        }
    }
}

