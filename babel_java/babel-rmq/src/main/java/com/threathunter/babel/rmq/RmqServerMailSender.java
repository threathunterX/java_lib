package com.threathunter.babel.rmq;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailEncoder;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailSender;
import com.threathunter.common.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.threathunter.babel.rmq.RmqConstant.*;

/**
 * Used for send response to client from server
 *
 * created by www.threathunter.cn
 */
public class RmqServerMailSender extends RmqBaseConnection implements MailSender {
    private static final Logger logger = LoggerFactory.getLogger(RmqServerMailSender.class);

    private final MailEncoder coder;

    public RmqServerMailSender(MailEncoder coder) {
        Utility.argumentNotEmpty(coder, "coder is null");

        this.coder = coder;
    }

    @Override
    public void sendMail(Mail m) throws MailException {
        if (m == null)
            return;

        try {
            String datacenter = m.getHeader("cdc");
            if (datacenter == null) {
                throw new RuntimeException("rmq:rpc:server:missing client datacenter in header");
            }
            String exchange = String.format("%s._client", datacenter);
            for (String to : m.getTo()) {
                getChannel().basicPublish(exchange, to, null, coder.encode(m).getBytes());
            }
        } catch (Exception e) {
            logger.error(String.format("rabbitmq:rpc:rmq server mail sender: publish error, to: %s", m.getTo().toString()), e);
            throw new MailException(e);
        }
    }

    @Override
    public void closeConnection() {
        closeChannelAndConnection();
    }
}
