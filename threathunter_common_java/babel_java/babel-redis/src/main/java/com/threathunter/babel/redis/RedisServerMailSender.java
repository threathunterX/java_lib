package com.threathunter.babel.redis;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailEncoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailSender;
import com.threathunter.common.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Used for send response to client from server
 *
 * @author Wen Lu
 */
public class RedisServerMailSender implements MailSender {
    private static final Logger logger = LoggerFactory.getLogger(RedisServerMailSender.class);

    private final MailEncoder coder;

    public RedisServerMailSender(MailEncoder coder) {
        Utility.argumentNotEmpty(coder, "coder is null");

        this.coder = coder;
    }

    @Override
    public void sendMail(Mail m) throws MailException {
        if (m == null)
            return;

        try {
            for (String to : m.getTo()) {
                RedisCtx.getRedisClient().rpush(to, coder.encode(m));
            }
        } catch (Exception e) {
            logger.error(String.format("redis:rpc:redis server mail sender: publish error, to: %s", m.getTo().toString()), e);
            throw new MailException(e);
        }
    }

    @Override
    public void closeConnection() {
    }
}
