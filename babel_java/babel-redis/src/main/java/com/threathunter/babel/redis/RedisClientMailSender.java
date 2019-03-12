package com.threathunter.babel.redis;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailEncoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailSender;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.util.DeliverMode;
import com.threathunter.common.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Used to send request to server.
 *
 * created by www.threathunter.cn
 */
public class RedisClientMailSender implements MailSender {

    private static final Logger logger = LoggerFactory.getLogger(RedisClientMailSender.class);

    private AtomicLong sendCount = new AtomicLong(0);

    private final MailEncoder coder;
    private final ServiceMeta meta;
    private final long cardinality;

    public RedisClientMailSender(ServiceMeta meta, MailEncoder coder) {
        Utility.argumentNotEmpty(coder, "coder is null");
        Utility.argumentNotEmpty(meta, "service meta is null");

        this.coder = coder;
        this.meta = meta;
        this.cardinality = ((Number)meta.getOptions().getOrDefault("servercardinality", 1L)).longValue();
    }

    @Override
    public void sendMail(Mail m) throws MailException {
        if (m == null)
            return;

        try {
            DeliverMode mode = DeliverMode.fromString(this.meta.getDeliverMode());

            if (mode == DeliverMode.QUEUE || mode == DeliverMode.SHUFFLE) {
                RedisCtx.getRedisClient().rpush(meta.getName(), coder.encode(m));
            } else if (mode == DeliverMode.TOPIC) {
                RedisCtx.getRedisClient().publish(meta.getName(), coder.encode(m));
            } else if (mode == DeliverMode.SHARDING) {
                String key = m.getHeader("key");
                long index = Math.abs(key.hashCode()) % cardinality;
                String list_name = String.format("%s.%d", meta.getName(), (index + 1));
                RedisCtx.getRedisClient().rpush(list_name, coder.encode(m));
            } else {
                throw new MailException("unsupported deliver mode");
            }
            sendCount.incrementAndGet();
        } catch (Exception e) {
            logger.error(String.format("redis:rpc:redis mail sender: publish error, to: %s", meta.getName()), e);
            throw new MailException(e);
        }
    }

    @Override
    public void closeConnection() {
    }
}
