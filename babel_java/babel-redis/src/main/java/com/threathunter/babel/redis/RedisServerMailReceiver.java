package com.threathunter.babel.redis;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailDecoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailReceiver;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.util.DeliverMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Receives request from the client.
 *
 * Created by daisy on 2015/6/19.
 *
 * 从consuming_name中拉取数据，并放到自有属性queue中
 * consuming_name 从配置文件中读取，即name字段，如：
 * {
 *     "name": "clickstreamquery",
 *     "callmode": "rpc",
 *     "delivermode": "sharding",
 *     "serverimpl": "redis",
 *     "coder": "mail",
 *     "options": {
 *         "cdc": "sh",
 *         "sdc": "sh",
 *         "servercardinality": 1,
 *         "serverseq": 1
 *     }
 */
public class RedisServerMailReceiver implements MailReceiver {

    private final static Logger logger = LoggerFactory.getLogger(RedisServerMailReceiver.class);
    private final static int DEFAULT_MAX_CACHE = 1000;

    private MailDecoder coder;
    private final ServiceMeta meta;

    private BlockingQueue<Mail> queue;
    private volatile boolean running = false;
    private volatile CountDownLatch hasSubscribed;
    private Thread receiver;
    private final String consuming_name;
    private final DeliverMode mode;
    private JedisPubSub pubsub;

    public RedisServerMailReceiver(ServiceMeta meta, MailDecoder coder, int maxCachedItem) throws IOException {
        this.meta = meta;
        this.coder = coder;

        if (maxCachedItem <= 0) {
            maxCachedItem = DEFAULT_MAX_CACHE;
        }

        this.queue = new ArrayBlockingQueue<>(maxCachedItem);
        this.mode = DeliverMode.fromString(meta.getDeliverMode());
        if (this.mode == DeliverMode.SHUFFLE || this.mode == DeliverMode.QUEUE || this.mode == DeliverMode.TOPIC) {
            consuming_name = meta.getName();
        } else if (this.mode == DeliverMode.SHARDING) {
            int seq = ((Number)meta.getOptions().get("serverseq")).intValue();
            consuming_name = String.format("%s.%d", meta.getName(), seq);
        } else {
            throw new RuntimeException("unsupport deliver mode");
        }
    }

    public RedisServerMailReceiver(ServiceMeta meta, MailDecoder coder) throws IOException {
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
                        logger.debug("start consume");
                        startConsume();
                    } catch(Throwable e) {
                        if (e.getClass().equals(InterruptedException.class))
                        logger.error("redis:rpc:redis consume error for service : " + meta.getName() + " exception", e);
                        Thread.interrupted();
                        continue;
                    }
                }
            }
        };
        receiver.setDaemon(true);
        receiver.start();

        try {
            if (!hasSubscribed.await(2, TimeUnit.SECONDS)) {
                logger.error("redis:rpc:{} time out while waiting for the subscribing thread", meta.getName());
            }
        } catch (InterruptedException e) {
            logger.error("redis:rpc:{} fail to wait for the count down latch", meta.getName());
        }
    }

    @Override
    public void stopReceiving() throws IOException {
        running = false;

        if (pubsub != null) {
            if (pubsub.isSubscribed()) {
                pubsub.unsubscribe();
            }
            pubsub = null;
        }

        logger.debug("receiving: " + serverCount);
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
            logger.error("redis:rpc:interrupted during the mail fetching");
            return null;
        }
    }

    public int getCachedMailCount() {
        return queue.size();
    }

    private AtomicLong serverCount = new AtomicLong(0);
    private void startConsume() throws IOException, InterruptedException {
        while (running) {
            if (this.mode == DeliverMode.TOPIC) {
                consumePubSub();
            } else {
                consumeList();
            }
        }
    }

    private void consumeList() {
        long overflow = 0;
        if (hasSubscribed != null)
            hasSubscribed.countDown();
        while (running) {
            List<String> result = RedisCtx.getRedisClient().blpop(1, consuming_name);
            if (result == null || result.size() <= 0)
                continue;

            serverCount.incrementAndGet();
            String msg = result.get(1);
            Mail m = this.coder.decode(msg);
            if (m == null) {
                logger.error("redis:rpc:corrupted mail");
                return;
            }

            if (!queue.offer(m)) {
                overflow++;
                if (overflow <= 10 || overflow % 1000 == 0) {
                    logger.error("redis:rpc:mail box full");
                }
            } else {
                overflow = 0; //reset after success
            }
        }
    }

    private void consumePubSub() {
        pubsub = new JedisPubSub() {
            // over flow events currently
            private long overflow = 0;

            @Override
            public void onMessage(String channel, String message) {
//                logger.debug("mail receiver get message {}", message);
                serverCount.incrementAndGet();
                Mail m = coder.decode(message);
                if (m == null) {
                    logger.error("redis:rpc:corrupted mail");
                    return;
                }

                if (!queue.offer(m)) {
                    overflow++;
                    if (overflow <= 10 || overflow % 1000 == 0) {
                        logger.error("redis:rpc:mail box full");
                    }
                } else {
                    overflow = 0; //reset after success
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                if (hasSubscribed != null)
                    hasSubscribed.countDown();
            }
        };

        RedisCtx.getRedisClient().subscribe(pubsub, consuming_name);
    }

}
