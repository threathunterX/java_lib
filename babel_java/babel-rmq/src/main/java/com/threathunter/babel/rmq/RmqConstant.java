package com.threathunter.babel.rmq;

/**
 * @author Wen Lu
 */
public class RmqConstant {
    public static final String RMQ_EXCHANGE_NAME_TOPIC = "_topic";
    public static final String RMQ_EXCHANGE_NAME_QUEUE = "_queue";
    public static final String RMQ_EXCHANGE_NAME_SHARDING = "_sharding";
    public static final String RMQ_EXCHANGE_NAME_SHUFFLE = "_shuffle";
    public static final String RMQ_EXCHANGE_NAME_TOFFLE = "_toffle";
    public static final String RMQ_EXCHANGE_NAME_TOSHARD = "_toshard";
    public static final String RMQ_EXCHANGE_NAME_CLIENT = "_client";

    public static final int RMQ_RECONNECT_WAIT_TIME = 3000;
}
