package com.threathunter.babel.rpc.impl;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailReceiver;
import com.threathunter.babel.mail.MailSender;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rmq.RmqUtil;
import com.threathunter.babel.rpc.HashUtils;
import com.threathunter.babel.rpc.MailUtil;
import com.threathunter.babel.rpc.RemoteException;
import com.threathunter.babel.rpc.ServiceClient;
import com.threathunter.babel.util.BabelMetricsHelper;
import com.threathunter.babel.util.DeliverMode;
import com.threathunter.babel.util.LocalServiceRegistry;
import com.threathunter.config.CommonDynamicConfig;
import com.threathunter.model.Event;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by daisy on 2015/7/9.
 * 向redis投递消息，并从clientId  key中读取处理结果
 */
public class ServiceClientImpl implements ServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceClientImpl.class);

    private AtomicLong requestid = new AtomicLong();
    private MailReceiver receiver;
    private MailSender sender;
    private ServiceMeta smeta;

    private String clientId;
    private final boolean batchEnable;

    public ServiceClientImpl(ServiceMeta meta, String clientName) {
        this(String.format("_client.%s.%s", meta.getName(), clientName), meta);
    }

    public ServiceClientImpl(ServiceMeta meta) {
        this(String.format("_client.%s.%s", meta.getName(), RandomStringUtils.randomAlphanumeric(10)), meta);
    }

    private ServiceClientImpl(String clientId, ServiceMeta meta) {
        this.smeta = meta;
        this.clientId = clientId;
        try {
            receiver = ConnectSysImpl.getInstance().getClientReceiver(this.clientId, meta);
            sender = ConnectSysImpl.getInstance().getClientSender(smeta);
        } catch (Exception e) {
            addErrorMetrics("createclient");
            throw new RuntimeException(e);
        }
        requestid.set(new Random().nextLong());
        this.batchEnable = CommonDynamicConfig.getInstance().getBoolean("babel_batch_enable", false);
    }

    // need manual bind service every time when change service
    @Override
    public void bindService(ServiceMeta meta) {
        if (this.smeta != null && meta.getName().equals(this.smeta.getName())) {
            return;
        }
        this.smeta = meta;
        try {
            sender = ConnectSysImpl.getInstance().getClientSender(smeta);
            // receiver should not change
//            receiver = ConnectSysImpl.getInstance().getClientReceiver(clientId, smeta);
        } catch (Exception e) {
            addErrorMetrics("bindservice");
            throw new RuntimeException(e);
        }
    }

    @Override
    /**
    * 将Mail发送到redis，并从clientid读取结果
    */
    public Event rpc(Event request, String destination, long timeout, TimeUnit unit) throws RemoteException {
        String rid = getNextReqeustid();

        long startTime = System.currentTimeMillis();
        long expireTimestamp = startTime + unit.toMillis(timeout);

        Map<String, String> headers = new HashMap<>();
        headers.put("ver", "0");
        headers.put("oneway", "false");
        headers.put("expire", String.valueOf(expireTimestamp + 5000));/*in case the distributed clock difference*/

        Mail preparedMail = prepareMail(request, destination, rid, headers);
        sendPreparedMail(preparedMail);

        Event result = getRPCResultMail(rid, expireTimestamp);

        addCostRangeMetrics(System.currentTimeMillis() - startTime);
        return result;
    }

    @Override
    public List<Event> polling(Event request, String destination, long timeout, TimeUnit unit) throws RemoteException {
        String rid = getNextReqeustid();

        long startTime = System.currentTimeMillis();
        long expireTimestamp = startTime + unit.toMillis(timeout);
        int cardinality = getCardinality();

        Map<String, String> headers = new HashMap<>();
        headers.put("ver", "0");
        headers.put("oneway", "false");
        headers.put("expire", String.valueOf(expireTimestamp + 5000));

        final List<Event> results = new ArrayList<>();
        Mail preparedMail = prepareMail(request, destination, rid, headers);
        sendPreparedMail(preparedMail);

        for (int i = cardinality; i > 0; i--) {
            try {
                results.add(getRPCResultMail(rid, expireTimestamp));
            } catch (Exception ex) {
                // ignore
            }
        }

        addCostRangeMetrics(System.currentTimeMillis() - startTime);
        return results;
    }

    @Override
    public void notify(Event request, String destination, long timeout, TimeUnit unit) throws RemoteException {
        String rid = getNextReqeustid();

        long startTime = System.currentTimeMillis();

        Map<String, String> headers = new HashMap<>();
        headers.put("ver", "0");
        headers.put("oneway", "true");
        if (timeout > 0) {
            headers.put("expire", String.valueOf(startTime + unit.toMillis(timeout)));
        }

        Mail preparedMail = prepareMail(request, destination, rid, headers);

        sendPreparedMail(preparedMail);

        addCostRangeMetrics(System.currentTimeMillis() - startTime);
    }

    @Override
    public void notify(Event request, String destination) throws RemoteException {
        notify(request, destination, -1, TimeUnit.SECONDS);
    }

    @Override
    public void notify(List<Event> requests, String destination, long timeout, TimeUnit unit) throws RemoteException {
        Map<String, String> headers = new HashMap<>();
        headers.put("ver", "0");
        headers.put("oneway", "true");

        DeliverMode mode = DeliverMode.fromString(this.smeta.getDeliverMode());
        if (mode == DeliverMode.SHARDING || mode == DeliverMode.TOPICSHARDING) {
            if (batchEnable) {
                this.shardBatchNotify(requests, destination, headers);
                return;
            } else {
                for (Event event : requests) {
                    this.notify(event, destination, timeout, unit);
                }
                return;
            }
        }

        String rid = getNextReqeustid();
        long startTime = System.currentTimeMillis();

        if (timeout > 0) {
            headers.put("expire", String.valueOf(startTime + unit.toMillis(timeout)));
        }

        Mail preparedMail = prepareMultiEventMail(requests, destination, rid, headers);
        sendPreparedMail(preparedMail);

        addCostRangeMetrics(System.currentTimeMillis() - startTime);
    }

    /**
     * for notify only, with compress, no shard, shard need to use shardBatchNotify
     * @param requests
     * @param destination
     * @throws RemoteException
     */
    @Override
    public void notify(List<Event> requests, String destination) throws RemoteException {
        notify(requests, destination, -1, TimeUnit.SECONDS);
    }

    private void shardBatchNotify(List<Event> requests, String destination, Map<String, String> headers) throws RemoteException {
        String rid = getNextReqeustid();

        long startTime = System.currentTimeMillis();

        int cardinality = 0;
        try {
            cardinality = ((Number)this.smeta.getOptions().get("servercardinality")).intValue();
        } catch (Exception e) {
            addErrorMetrics("misscardinality");
            dealWithError("missing server cardinality", e);
        }

        Map<Integer, List<Event>> requestMailsMap = getSlotedMailsMap(requests, cardinality);

        List<Mail> preparedMails = new ArrayList<>();
        for (Map.Entry<Integer, List<Event>> entry : requestMailsMap.entrySet()) {
            Map<String, String> singleMailHeaders = new HashMap<>();
            singleMailHeaders.putAll(headers);
            singleMailHeaders.put("key", LocalServiceRegistry.getInstance().getRmqShardNumber(entry.getKey()));

            Mail requestMail = prepareMultiEventMail(entry.getValue(), destination, rid, singleMailHeaders);

            preparedMails.add(requestMail);
        }

        for (Mail mail : preparedMails) {
            sendPreparedMail(mail);
        }
        addCostRangeMetrics(System.currentTimeMillis() - startTime);
    }

    private Map<Integer, List<Event>> getSlotedMailsMap(List<Event> requests, int totalShard) {
        Map<Integer, List<Event>> listMap = new HashMap<>();
        for (Event event : requests) {
            int mod = getShard(event.getKey(), totalShard);
            if (!listMap.containsKey(mod)) {
                listMap.put(mod, new ArrayList<>());
            }
            listMap.get(mod).add(event);
        }

        return listMap;
    }

    /**
     * This method only apply to non-shard multi mail request
     * @param requests
     * @param destination
     * @param rid
     * @param headers
     * @return
     */
    private Mail prepareMultiEventMail(List<Event> requests, String destination, String rid, Map<String, String> headers) {
        try {
            List<String> to = Arrays.asList(destination);
            List<String> reply = Arrays.asList();
            byte[] body = null;

            if (smeta.getOption("sdc") == null && smeta.getOption("cdc") == null) {
                throw new RuntimeException("rmq:client:send:missing sdc or cdc");
            }
            headers.put("sdc", (String) smeta.getOption("sdc"));
            headers.put("cdc", (String) smeta.getOption("cdc"));

            Mail preparedMail = new Mail(this.clientId, to, reply, rid, headers, body);

            MailUtil.populateEventListIntoMail(preparedMail, requests);
            return preparedMail;
        } catch (Exception e) {
            addErrorMetrics("preparemail");
            throw new RuntimeException("client error: prepare mail error, destination: "+destination, e);
        }
    }

    @Override
    public void start() {
        receiver.startReceiving();
    }

    @Override
    public void stop() {
        try {
            receiver.stopReceiving();
            sender.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    private Mail prepareMail(Event request, String destination, String rid, Map<String, String> headers) {
        try {
            List<String> to = Arrays.asList(destination);
            List<String> reply = Arrays.asList(this.clientId);
            DeliverMode mode = DeliverMode.fromString(this.smeta.getDeliverMode());
            if (mode == DeliverMode.SHARDING || mode == DeliverMode.TOPICSHARDING) {
                int cardinality = 0;
                try {
                    cardinality = ((Number)this.smeta.getOptions().get("servercardinality")).intValue();
                } catch (Exception e) {
                    dealWithError("missing server cardinality", e);
                }
                if (batchEnable) {
                    headers.put("key", LocalServiceRegistry.getInstance().getRmqShardNumber(getShard(request.getKey(), cardinality)));
                } else {
                    headers.put("key", HashUtils.getHash(request.getKey()));
                }
            }

            byte[] body = null;

            if (smeta.getOption("sdc") == null || smeta.getOption("cdc") == null) {
                throw new RuntimeException("rmq:client:send:missing sdc or cdc");
            }

            headers.put("sdc", (String) smeta.getOption("sdc"));
            headers.put("cdc", (String) smeta.getOption("cdc"));
            Mail preparedMail = new Mail(this.clientId, to, reply, rid, headers, body);

            MailUtil.populateEventIntoMail(preparedMail, request);
            return preparedMail;
        } catch (Exception e) {
            addErrorMetrics("preparemail");
            throw new RuntimeException("client error: prepare mail error, destination: "+destination, e);
        }
    }

    private void sendPreparedMail(Mail preparedMail) throws RemoteException {
        receiver.drainMail(); // in case there are stale mail.
        try {
            sender.sendMail(preparedMail);
            addSendMetrics(true);
        } catch (MailException ex) {
            addErrorMetrics("senderror");
            addSendMetrics(false);
            dealWithError("fail to send request mail", ex);
        }
    }

    private Event getRPCResultMail(String rid, long expireTimestamp) throws RemoteException {
        Mail resultMail = null;
        while (true) {
            long current = System.currentTimeMillis();
            if (current > expireTimestamp)
                break;

            try {
                resultMail = receiver.getMail(expireTimestamp - current, TimeUnit.MILLISECONDS);
            } catch (MailException ex) {
                addErrorMetrics("servererror");
                dealWithError("fail to get response mail", ex);
            }

            if (resultMail != null && resultMail.getRequestid().equals(rid)) {
                break;
            }
        }

        if (resultMail == null || !(rid.equals(resultMail.getRequestid()))) {
            addErrorMetrics("timeout");
            dealWithError("timeout while waiting for the response");
        }

        return MailUtil.extractEventsFromMail(resultMail).get(0);
    }

    // get the total number of the polling events...
    private int getCardinality() throws RemoteException {
        int cardinality = 0;
        try {
            cardinality = ((Number)this.smeta.getOptions().get("servercardinality")).intValue();
        } catch (Exception e) {
            addErrorMetrics("misscardinality");
            dealWithError("missing server cardinality", e);
        }
        return cardinality;
    }

    private String getNextReqeustid() {
        return this.clientId + "_" + requestid.getAndIncrement();
    }

    private void dealWithError(String errorMsg) throws RemoteException {
        errorMsg = "rpc:client:" + errorMsg;
        logger.error(errorMsg);
        throw new RemoteException(errorMsg);
    }

    private void dealWithError(String errorMsg, Throwable th) throws RemoteException {
        errorMsg = "rpc:client:" + errorMsg;
        logger.error(errorMsg, th);
        throw new RemoteException(errorMsg);
    }

    private int getShard(String key, int totalShard) {
        long hash = RmqUtil.getHash(key);
        return (int)(hash % totalShard);
    }

    private void addErrorMetrics(String type) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("type", type);
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.client.error", tagMap, 1.0);
    }
    private void addSendMetrics(boolean isSucess) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("success", isSucess);
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.client.sendcount", tagMap, 1.0);
    }
    private void addCostRangeMetrics(long cost) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("range", BabelMetricsHelper.getInstance().getRangeLabel(cost));
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.client.costrange", tagMap, 1.0);
    }

    private Map<String, Object> getBasicTagMap() {
        Map<String, Object> tags = new HashMap<>();
        tags.put("clientid", this.clientId);
        tags.put("impl", smeta.getServerImpl());
        tags.put("delivery", smeta.getDeliverMode());
        tags.put("call", smeta.getCallMode());
        tags.put("service", smeta.getName());
        tags.put("sdc", smeta.getOptionOrDefault("sdc", ""));
        tags.put("cdc", smeta.getOptionOrDefault("cdc", ""));
        return tags;
    }
}
