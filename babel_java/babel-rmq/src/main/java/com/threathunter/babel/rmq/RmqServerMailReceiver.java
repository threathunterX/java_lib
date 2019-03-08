package com.threathunter.babel.rmq;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailDecoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailReceiver;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.util.DeliverMode;
import com.threathunter.config.CommonDynamicConfig;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.threathunter.babel.rmq.RmqConstant.*;

/**
 * Receives request from the client.
 *
 * Created by daisy on 2015/6/19.
 */
public class RmqServerMailReceiver extends RmqBaseConnection implements MailReceiver {

    private final static Logger logger = LoggerFactory.getLogger(RmqServerMailReceiver.class);
    private final static int DEFAULT_MAX_CACHE = CommonDynamicConfig.getInstance().getInt("babel_events_capacity", 10000);
    private final static String SHARDING_WEIGH = CommonDynamicConfig.getInstance().getString("babel_sharding_weigh", "10");

    private MailDecoder coder;
    private final ServiceMeta meta;

    private BlockingQueue<Mail> queue;
    private volatile boolean running = false;
    private volatile CountDownLatch hasSubscribed;
    private Thread receiver;

    private QueueingConsumer consumer;

    private String consuming_queue;

    public RmqServerMailReceiver(ServiceMeta meta, MailDecoder coder, int maxCachedItem) throws IOException {
        this.meta = meta;
        this.coder = coder;

        if (maxCachedItem <= 0) {
            maxCachedItem = DEFAULT_MAX_CACHE;
        }

        this.queue = new ArrayBlockingQueue<>(maxCachedItem);
        setupConsumer();
    }

    public RmqServerMailReceiver(ServiceMeta meta, MailDecoder coder) throws IOException {
        this(meta, coder, DEFAULT_MAX_CACHE);
    }

    @Override
    public void startReceiving() {
        if (running) {
            logger.warn("misc:already start");
            return;
        }
        running = true;
        hasSubscribed = new CountDownLatch(1);

        receiver = new Thread("consume for " + meta.getName()) {
            @Override
            public void run() {
                while (running) {
                    try {
                        startConsume();
                    } catch(Throwable e) {
                        if (e.getClass().equals(ShutdownSignalException.class) || e.getClass().equals(ConnectException.class) || e.getClass().equals(SocketTimeoutException.class)) {
                            try {
                                destroyConnection();
                                consumer = null;
                                Thread.sleep(RmqConstant.RMQ_RECONNECT_WAIT_TIME);
                            } catch (Exception e1) {
                                logger.error("rabbitmq:rpc:reset connection error", e);
                            }
                        }
                        if (e.getClass().equals(InterruptedException.class)) {
                            logger.error("rabbitmq:rpc:rmq consume error for service : " + meta.getName() + " exception", e);
                        }
                        Thread.interrupted();
                        continue;
                    }
                }
            }
        };
        receiver.setDaemon(true);
        receiver.start();

        try {
            if (!hasSubscribed.await(1, TimeUnit.SECONDS)) {
                logger.error("rabbitmq:rpc:time out while waiting for the subscribing thread");
            }
        } catch (InterruptedException e) {
            logger.error("rabbitmq:rpc:fail to wait for the count down latch");
        }
    }

    @Override
    public void stopReceiving() throws IOException {
        running = false;

        if (receiver != null) {
            try {
                receiver.join(2000/*milliseconds*/);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            closeChannelAndConnection();
        }
    }

    @Override
    public List<Mail> drainMail() {
        List<Mail> result = new ArrayList<Mail>(queue.size());
        queue.drainTo(result);
        return result;
    }

    @Override
    public Mail getMail() throws MailException {
        if (!consumer.getChannel().isOpen() && queue.isEmpty()) {
            throw new MailException("it is not subsribing now");
        }
        return queue.poll();
    }

    @Override
    public Mail getMail(long timeout, TimeUnit unit) throws MailException {
        if (!consumer.getChannel().isOpen() && queue.isEmpty()) {
            throw new MailException("it is not consuming now");
        }
        try {
            return queue.poll(timeout, unit);
        } catch (InterruptedException e) {
            logger.error("rabbitmq:rpc:interrupted during the mail fetching");
            return null;
        }
    }

    private void setupConsumer() {
        if (this.consumer != null) {
            return;
        }
        DeliverMode mode = DeliverMode.fromString(meta.getDeliverMode());
        try {
            Object clientDc = meta.getOptions().get("sdc");
            if (clientDc == null) {
                throw new RuntimeException("rmp:server:missing client datacenter in option.");
            }
            String exchangeName = String.format("%s._client", clientDc);
            getChannel().exchangeDeclare(exchangeName, "direct", true, false, null);

            if (mode == DeliverMode.SHARDING) {
                setupShardingConsumer();
            } else if (mode == DeliverMode.TOPICSHARDING) {
                setupTopicShardingConsumer();
            } else if (mode == DeliverMode.QUEUE) {
                setupQueueConsumer();
            } else if (mode == DeliverMode.TOPIC) {
                setupTopicConsumer();
            } else if (mode == DeliverMode.SHUFFLE) {
                setupShuffleConsumer();
            } else if (mode == DeliverMode.TOPICSHUFFLE) {
                setupTopicShuffleConsumer();
            } else {
                throw new RuntimeException("invalid deliver mode");
            }

            this.consumer = new QueueingConsumer(getChannel());
        } catch (Exception e) {
            logger.debug("rmq:receiver: initial consumer error, service : " + meta.getName(), e);
        }
    }


    private void setupTopicShuffleConsumer() throws IOException {
        String exchange = String.format("%s.%s", meta.getOptions().get("sdc"), RMQ_EXCHANGE_NAME_TOFFLE);
        String sname = meta.getName();
        String subname = (String) meta.getOptions().get("serversubname");
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }
        this.consuming_queue = String.join(".", sname, subname);

        getChannel().exchangeDeclare(exchange, "topic", true, false, null);
        getChannel().queueDeclare(consuming_queue, durable, false, autoDelete, null);
        getChannel().queueBind(consuming_queue, exchange, sname);
    }

    private void setupShuffleConsumer() throws IOException {
        String exchange = String.format("%s.%s", meta.getOptions().get("sdc"), RMQ_EXCHANGE_NAME_SHUFFLE);
        String sname = meta.getName();
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }
        this.consuming_queue = sname;

        getChannel().exchangeDeclare(exchange, "direct", true, false, null);
        getChannel().queueDeclare(sname, durable, false, autoDelete, null);
        getChannel().queueBind(sname, exchange, sname);
    }

    private void setupTopicConsumer() throws IOException {
        String exchange = String.format("%s.%s", meta.getOptions().get("sdc"), RMQ_EXCHANGE_NAME_TOPIC);
        String sname = meta.getName();
        String subname = (String) meta.getOptions().get("serversubname");
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }
        this.consuming_queue = String.join(".", sname, subname);

        getChannel().exchangeDeclare(exchange, "topic", true, false, null);
        getChannel().queueDeclare(this.consuming_queue, durable, false, autoDelete, null);
        getChannel().queueBind(this.consuming_queue, exchange, sname);
    }

    private void setupQueueConsumer() throws IOException {
        String exchange = String.format("%s.%s", meta.getOptions().get("sdc"), RMQ_EXCHANGE_NAME_QUEUE);
        String sname = meta.getName();
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }
        this.consuming_queue = sname;

        getChannel().exchangeDeclare(exchange, "direct", true, false, null);
        getChannel().queueDeclare(sname, durable, false, autoDelete, null);
        getChannel().queueBind(sname, exchange, sname);
    }

    private void setupTopicShardingConsumer() throws IOException {
        String sname = meta.getName();
        String seq = meta.getOptions().get("serverseq").toString();
        String subname = (String) meta.getOptions().get("serversubname");
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }

        String exchange = String.format("%s.%s.%s", meta.getOptions().get("sdc"), RMQ_EXCHANGE_NAME_TOSHARD, sname);
        String exchangeSub = String.join(".", sname, subname);
        this.consuming_queue = String.join(".", sname, subname, seq);

        getChannel().exchangeDeclare(exchange, "fanout", durable, autoDelete, null);
        getChannel().exchangeDeclare(exchangeSub, "x-consistent-hash", durable, autoDelete, null);
        // TODO sname + ".*"
        getChannel().exchangeBind(exchangeSub, exchange, "*");
        getChannel().queueDeclare(consuming_queue, durable, false, autoDelete, null);
        getChannel().queueBind(consuming_queue, exchangeSub, SHARDING_WEIGH);
    }

    private void setupShardingConsumer() throws IOException {
        String sname = meta.getName();
        String exchange = String.format("%s.%s.%s", meta.getOptions().get("sdc"), RMQ_EXCHANGE_NAME_SHARDING, sname);
        String seq = meta.getOptions().get("serverseq").toString();
        boolean durable = false;
        boolean autoDelete = true;
        if (meta.getOptions().get("durable") != null) {
            durable = (Boolean) meta.getOptions().get("durable");
            autoDelete = !durable;
        }
        this.consuming_queue = String.join(".", sname, seq);

        getChannel().exchangeDeclare(exchange, "x-consistent-hash", durable, autoDelete, null);
        getChannel().queueDeclare(consuming_queue, durable, false, autoDelete, null);
        getChannel().queueBind(consuming_queue, exchange, SHARDING_WEIGH);
    }

    private void startConsume() throws IOException, InterruptedException {
        // basicQos mush use together with autoAct false
        //TODO getChannel().basicQos(1), together with ack...
        setupConsumer();
        getChannel().basicConsume(this.consuming_queue, true, consumer);
        long overflow = 0;

        if (hasSubscribed != null)
            hasSubscribed.countDown();

        logger.debug("start consume");
        while (running) {
            QueueingConsumer.Delivery delivery = this.consumer.nextDelivery(100);
            if (delivery == null)
                continue;
            String message = new String(delivery.getBody());
            Mail m = this.coder.decode(message);
            if (m == null) {
                logger.error("rabbitmq:rpc:corrupted mail");
                return;
            }

            if (!queue.offer(m)) {
                overflow++;
                if (overflow <= 10 || overflow % 1000 == 0) {
                    logger.error("rabbitmq:rpc:mail box full");
                }
            } else {
                overflow = 0; //reset after success
            }
        }
    }

}
