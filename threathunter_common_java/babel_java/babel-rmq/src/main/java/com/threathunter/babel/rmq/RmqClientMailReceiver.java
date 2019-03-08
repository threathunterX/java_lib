package com.threathunter.babel.rmq;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailDecoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailReceiver;
import com.threathunter.babel.meta.ServiceMeta;
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
 * Receive response from Server.
 *
 * @author Wen Lu
 */
public class RmqClientMailReceiver extends RmqBaseConnection implements MailReceiver {
    private final static Logger logger = LoggerFactory.getLogger(RmqClientMailReceiver.class);
    private final static int DEFAULT_MAX_CACHE = 1000;

    private final MailDecoder coder;
    private final String clientid;
    private final ServiceMeta smeta;

    private BlockingQueue<Mail> queue;
    private volatile boolean running = false;
    private volatile boolean reconnecting = false;
    private volatile CountDownLatch hasSubscribed;
    private Thread receiver;

    protected QueueingConsumer consumer;

    public RmqClientMailReceiver(String clientId, MailDecoder coder, ServiceMeta meta, int maxCachedItem) throws IOException {
        this.coder = coder;
        this.clientid = clientId;
        this.smeta = meta;

        if (maxCachedItem <= 0) {
            maxCachedItem = DEFAULT_MAX_CACHE;
        }

        this.queue = new ArrayBlockingQueue<>(maxCachedItem);
    }

    public RmqClientMailReceiver(String clientId, MailDecoder coder, ServiceMeta meta) throws IOException {
        this(clientId, coder, meta, DEFAULT_MAX_CACHE);
    }

    @Override
    public void startReceiving() {
        if (running) {
            logger.warn("misc:already start");
            return;
        }
        running = true;
        hasSubscribed = new CountDownLatch(1);

        receiver = new Thread("consume for client " + this.clientid) {
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
                                reconnecting = true;
                                Thread.sleep(RmqConstant.RMQ_RECONNECT_WAIT_TIME);
                            } catch (Exception e1) {
                                logger.error("rabbitmq:rpc:reset connection error", e);
                            }
                        }
                        if (e.getClass().equals(InterruptedException.class)) {
                            logger.error("rabbitmq:rpc:rmq consume error, client id: " + clientid + " exception", e);
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
        try {
            setupClientConsumer();
            this.consumer = new QueueingConsumer(getChannel());
            getChannel().basicConsume(this.clientid, true, consumer);
        } catch (Exception e) {
            logger.debug("rmq:receiver: initial consumer error, client id: " + this.clientid, e);
        }
    }

    private void setupClientConsumer() throws IOException {
        Object clientDc = smeta.getOptions().get("cdc");
        if (clientDc == null) {
            throw new RuntimeException("rmq:client:reveiver:missing client datacenter in option.");
        }
        String exchangeName = String.format("%s._client", clientDc);
        getChannel().exchangeDeclare(exchangeName, "direct", true, false, null);
        getChannel().queueDeclare(this.clientid, false, false, true, null);
        getChannel().queueBind(this.clientid, exchangeName, this.clientid);
    }

    private void startConsume() throws IOException, InterruptedException {
        setupConsumer();
        long overflow = 0;

        if (!reconnecting && hasSubscribed != null) {
            hasSubscribed.countDown();
        }
        reconnecting = false;
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
