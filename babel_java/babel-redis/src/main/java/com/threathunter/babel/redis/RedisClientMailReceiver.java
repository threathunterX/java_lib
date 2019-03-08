package com.threathunter.babel.redis;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailDecoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Receive response from Server.
 *
 * @author Wen Lu
 *
 * RedisClientMailReceiver
 *  从 redis中clientid 对应 key 中读取结果
 */
public class RedisClientMailReceiver implements MailReceiver {
    private final static Logger logger = LoggerFactory.getLogger(RedisClientMailReceiver.class);
    private final static int DEFAULT_MAX_CACHE = 1000;

    private final MailDecoder coder;
    private final String clientid;

    private BlockingQueue<Mail> queue;
    private volatile boolean running = false;
    private volatile CountDownLatch hasSubscribed;
    private Thread receiver;

    public RedisClientMailReceiver(String clientid, MailDecoder coder, int maxCachedItem) throws IOException {
        this.coder = coder;
        this.clientid = clientid;

        if (maxCachedItem <= 0) {
            maxCachedItem = DEFAULT_MAX_CACHE;
        }

        this.queue = new ArrayBlockingQueue<>(maxCachedItem);
    }

    public RedisClientMailReceiver(String clientid, MailDecoder coder) throws IOException {
        this(clientid, coder, DEFAULT_MAX_CACHE);
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
                        logger.debug("start consume");
                        startConsume();
                        logger.debug("exit consume");
                    } catch(Throwable e) {
                        if (e.getClass().equals(InterruptedException.class))
                            logger.error("redis:redis consume error, client id: " + clientid + " exception", e);
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
                logger.error("redis:time out while waiting for the subscribing thread");
            }
        } catch (InterruptedException e) {
            logger.error("redis:fail to wait for the count down latch");
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
        if (!running && queue.isEmpty()) {
            throw new MailException("it is not subsribing now");
        }
        return queue.poll();
    }

    @Override
    public Mail getMail(long timeout, TimeUnit unit) throws MailException {
        if (!running && queue.isEmpty()) {
            throw new MailException("it is not consuming now");
        }
        try {
            return queue.poll(timeout, unit);
        } catch (InterruptedException e) {
            logger.error("redis:interrupted during the mail fetching");
            return null;
        }
    }

    public int getCachedMailCount() {
        return queue.size();
    }

    private void startConsume() throws IOException, InterruptedException {
        long overflow = 0;

        if (hasSubscribed != null)
            hasSubscribed.countDown();

        while (running) {

            try {
                while (running) {
                    List<String> result = RedisCtx.getRedisClient().blpop(1, clientid);
                    if (result == null || result.size() <= 0) {
                        continue;
                    }
                    String msg = result.get(1);
                    Mail m = this.coder.decode(msg);
                    if (m == null) {
                        logger.error("redis:corrupted mail");
                        return;
                    }

                    if (!queue.offer(m)) {
                        overflow++;
                        if (overflow <= 10 || overflow % 1000 == 0) {
                            logger.error("redis:mail box full");
                        }
                    } else {
                        overflow = 0; //reset after success
                    }
                }
            } catch(Exception e) {
                logger.error("redis: error receive response", e);
            }
        }
    }
}
