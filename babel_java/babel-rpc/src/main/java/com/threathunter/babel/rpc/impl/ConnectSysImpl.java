package com.threathunter.babel.rpc.impl;

import com.threathunter.babel.mail.*;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.redis.*;
import com.threathunter.babel.rmq.*;
import com.threathunter.babel.util.ServiceConstant;
import java.io.IOException;

/**
 * created by www.threathunter.cn
 */
public class ConnectSysImpl {

    private static ConnectSysImpl connectSys = new ConnectSysImpl();

    public static ConnectSysImpl getInstance() {
        return connectSys;
    }

    public MailReceiver getServerReceiver(ServiceMeta meta) throws IOException {
        if (meta.getServerImpl().equals(ServiceConstant.RMQ_SERVER_NAME)) {
            return new RmqServerMailReceiver(meta, this.getDecoder(meta.getCoder()));
        } else if (meta.getServerImpl().equals(ServiceConstant.REDIS_SERVER_NAME)) {
            return new RedisServerMailReceiver(meta, this.getDecoder(meta.getCoder()));
        }
        throw new RuntimeException("server impl not supported yet");
    }

    public MailSender getServerSender(ServiceMeta meta) {
        if (meta.getServerImpl().equals(ServiceConstant.RMQ_SERVER_NAME)) {
            return new RmqServerMailSender(this.getEncoder(meta.getCoder()));
        } else if (meta.getServerImpl().equals(ServiceConstant.REDIS_SERVER_NAME)) {
            return new RedisServerMailSender(this.getEncoder(meta.getCoder()));
        }
        throw new RuntimeException("server impl not supported yet");
    }

    public MailReceiver getClientReceiver(String clientId, ServiceMeta meta) throws IOException {
        if (meta.getServerImpl().equals(ServiceConstant.RMQ_SERVER_NAME)) {
            return new RmqClientMailReceiver(clientId, this.getDecoder(meta.getCoder()), meta);
        } else if (meta.getServerImpl().equals(ServiceConstant.REDIS_SERVER_NAME)) {
            return new RedisClientMailReceiver(clientId, this.getDecoder(meta.getCoder()));
        }
        throw new RuntimeException("server impl not supported yet");
    }

    public MailSender getClientSender(ServiceMeta meta) throws IOException {
        if (meta.getServerImpl().equals(ServiceConstant.RMQ_SERVER_NAME)) {
            return new RmqClientMailSender(meta, this.getEncoder(meta.getCoder()));
        } else if (meta.getServerImpl().equals(ServiceConstant.REDIS_SERVER_NAME)) {
            return new RedisClientMailSender(meta, this.getEncoder(meta.getCoder()));
        }
        throw new RuntimeException("server impl not supported yet");
    }

    private MailEncoder getEncoder(String coder) {
        if (coder.equals("mail")) {
            return new JsonMailEncoderDecoder();
        }
        throw new RuntimeException("encoder is not supported: "+coder);
    }
    private MailDecoder getDecoder(String coder) {
        if (coder.equals("mail")) {
            return new JsonMailEncoderDecoder();
        }
        throw new RuntimeException("encoder is not supported: "+coder);
    }
}
