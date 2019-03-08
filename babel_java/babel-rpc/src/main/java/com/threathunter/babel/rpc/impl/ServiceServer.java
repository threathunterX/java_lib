package com.threathunter.babel.rpc.impl;

import com.threathunter.babel.mail.Mail;
import com.threathunter.babel.mail.MailException;
import com.threathunter.babel.mail.MailReceiver;
import com.threathunter.babel.mail.MailSender;
import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.MailUtil;
import com.threathunter.babel.rpc.RemoteException;
import com.threathunter.babel.rpc.Service;
import com.threathunter.babel.util.BabelMetricsHelper;
import com.threathunter.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by daisy on 2015/6/30.
 *
 * MailReceiver负责从redis/mq里读取数据（Mail），并放到自有queue中
 * acceptor从MailReceiver.queue中读取数据放入WorkerProcessor.processEvent中处理(调用的是addService所绑定服务的service.process)
 * 处理结果通过sender发回到Mail内reply指定的key中
 */
class ServiceServer {
    private static final Logger logger = LoggerFactory.getLogger(ServiceServer.class);
    private Service service;

    private MailReceiver receiver;
    private MailSender sender;
    private ExecutorService workers;
    private AcceptorThread acceptor;

    private volatile boolean running = false;
    // service server id, must be unique
    private String id;
    private final ServiceMeta meta;
    private final long defaultEventExpireSeconds;

    /**
     * Every time need to pass the FileLocation of the rabbitmq or redis connection config,
     * bug factory will initial once
     * @param id
     * @param service
     */
    public ServiceServer(String id, Service service) {
        meta = service.getServiceMeta();
        defaultEventExpireSeconds = Long.parseLong((String) meta.getOptionOrDefault("expire", "-1"));
        try {
            this.receiver = ConnectSysImpl.getInstance().getServerReceiver(meta);
            this.sender = ConnectSysImpl.getInstance().getServerSender(meta);
        } catch (Exception e) {
            addErrorMetrics("createserver");
            logger.error("rpc:create receiver and sender error: ", e);
        }
        this.id = id;

        this.workers = Executors.newFixedThreadPool(5);
    }

    public void putService(Service service) {
        this.service = service;
    }

    public void startWork() {
        if (running) {
            return;
        }
        running = true;
        receiver.startReceiving();
        acceptor = new AcceptorThread(id);
        acceptor.start();
    }

    public String getName() {
        return id;
    }

    public void stopWork() throws IOException {
        if (!running) {
            return;
        }
        running = false;
        try {
            receiver.stopReceiving();
            sender.closeConnection();

            service.close();
        } catch (IOException e) {
            addErrorMetrics("stopserver");
            logger.error("rpc:close:close service server error, workshop: " + id, e);
        }

        if (acceptor != null) {
            try {
                acceptor.join(2000);
            } catch (InterruptedException e) {
                logger.error("close:rpc:the acceptor join is interrupted, service server: " + id, e);
            }
            acceptor = null;
        }
        try {
            workers.shutdown();
            workers.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            addErrorMetrics("stopworker");
            logger.error("close:rpc:timeout while shutting down the workers: " + id, e);
        }

    }

    public String getId() {
        return id;
    }

    class AcceptorThread extends Thread {

        private int errors = 0;
        public AcceptorThread(String parent) {
            super(parent + ":Acceptor");
        }

        @Override
        public void run() {
            logger.debug("Acceptor({}) start working", id);

            while (running) {
                try {
                    final Mail m = receiver.getMail(500, TimeUnit.MILLISECONDS);
                    if (m != null) {
                        workers.submit(new WorkerProcessor(m));
                    }
                    errors = 0; // success this time
                } catch (Exception e) {
                    addErrorMetrics("accept");
                    errors++;
                    if (errors < 10 || errors % 1000 == 0)
                        logger.error("rpc:fatal:caught exception in the acceptor, workshop: " + id, e);
                    try {
                        Thread.sleep(500);
                        // wait
                    } catch (InterruptedException e1) {
                    }
                }
            }

            // deal with the remaining mails;
            for (Mail m : receiver.drainMail()) {
                try {
                    if (m != null) {
                        workers.submit(new WorkerProcessor(m));
                    }
                } catch (Exception e) {
                    addErrorMetrics("accept");
                    logger.error("rpc:fatal:caught exception in the acceptor: workshop: " + id, e);
                }
            }

            logger.debug("Acceptor({}) stop working", id);
        }
    }

    class WorkerProcessor implements Runnable {

        private final Mail m;
        private long startTimeMillis;

        WorkerProcessor(Mail m) {
            this.m = m;
            this.startTimeMillis = System.currentTimeMillis();
        }

        @Override
        public void run() {
            // get version for future use
            Long version = Long.valueOf(m.getHeader("ver", "0"));

            String requestid = m.getRequestid();
            boolean oneway = "notify".equalsIgnoreCase(meta.getCallMode());

            try {
                List<Event> events = MailUtil.extractEventsFromMail(m);

                if (!oneway && events.size() != 1) {
                    throw new RuntimeException("callable service should have exact one event in one call");
                }

                if (oneway) {
                    // notice

                    for (Event event : events) {
                        try {
                            processEvent(event);
                        } catch (Exception ex) {
                            addErrorMetrics("invokefunction", m.getFrom(), m.getHeader("cdc", ""));
                            // deal with other notices
                            continue;
                        }
                    }

                } else {
                    // service call
                    Event resultEvent = processEvent(events.get(0));
                    if (resultEvent == null) {
                        throw new RuntimeException("no result event received");
                    }
                    if (resultEvent.getName() == null || resultEvent.getName().isEmpty()) {
                        // if polling mode & empty event, then ignore
                        if (meta.getCallMode().equals("polling")) {
                            return;
                        }
                    }

                    List<String> to = m.getReply();
                    if (to.isEmpty()) {
                        to.add(m.getFrom());
                    }
                    List<String> reply = new ArrayList<>(); // this is response, don't need to write the reply
                    Map<String, String> headers = new HashMap<String, String>();
//                    String clientDc = m.getHeader("cdc");
//                    if (clientDc != null) {
//                        headers.put("cdc", clientDc);
//                    }
                    // TODO clientDc must exist
                    headers.put("cdc", m.getHeader("cdc", ""));
                    Mail returnMail = new Mail(service.getServiceMeta().getName(), to , reply, requestid, headers, "");
                    MailUtil.populateEventIntoMail(returnMail, resultEvent);

                    try {
                        sender.sendMail(returnMail);
                    } catch (MailException ex) {
                        addErrorMetrics("sendresponse", m.getFrom(), m.getHeader("cdc", ""));
                        dealWithError("rpc:fail to send response mail", ex);
                    }

                }

            } catch (Exception ex) {
                // fail to deal with the mail
                String errMsg = ex.getMessage();
                logger.warn(errMsg);
                try {
                    if (!oneway) {
                        sender.sendMail(buildErrorMail(service.getServiceMeta().getName(), m.getReply(), errMsg, requestid));
                    }
                } catch (Exception sendEx) {
                    addErrorMetrics("sendresponse", m.getFrom(), m.getHeader("cdc", ""));
                    logger.error(String.format("rpc:fail to deal with error msg: %s, workshop: %s", errMsg, id), sendEx);
                }
            }
            addCostRangeMetrics(System.currentTimeMillis() - this.startTimeMillis, m.getFrom(), m.getHeader("cdc", ""));
        }

        private Event processEvent(Event e) throws RemoteException {
            if (service == null) {
                String errMsg = "rpc:did not set service for event: " + e;
                dealWithError(errMsg);
            }

            // deal with timeout()
            long expire;
            if (defaultEventExpireSeconds > 0) {
                expire = e.getTimestamp() + defaultEventExpireSeconds * 1000;
            } else {
                expire = Long.MAX_VALUE;
            }
            String clientExpireStr = m.getHeader("expire");
            if (clientExpireStr != null) {
                expire = Long.parseLong(clientExpireStr);
            }
            if (expire < System.currentTimeMillis()) {
                // expire
                String errorMsg = String.format("the request should be processed " +
                        "before %d, but it is %d now, request name=" + e.getName(), expire, System.currentTimeMillis());
                addErrorMetrics("invoketoolate", m.getFrom(), m.getHeader("cdc", ""));
                logger.debug(errorMsg); // use debug level as there may be too many such messages while warm up
                throw new RemoteException(errorMsg);
            }

            Event result = null;
            try {
                result = service.process(e);
                addProcessMetrics(true, m.getFrom(), m.getHeader("cdc", ""));
            } catch (Exception ex) {
                addProcessMetrics(false, m.getFrom(), m.getHeader("cdc", ""));
                addErrorMetrics("invokefunction", m.getFrom(), m.getHeader("cdc", ""));
                String errorMsg = String.format("rpc:service %d processing event %d " +
                        "meets exception[%s]", service, e, ex.getMessage());
                dealWithError(errorMsg);
            }

            return result;
        }

        private void dealWithError(String errorMsg) throws RemoteException {
            logger.warn(errorMsg);
            throw new RemoteException(errorMsg);
        }

        private void dealWithError(String errorMsg, Throwable th) throws RemoteException {
            logger.warn(errorMsg);
            throw new RemoteException(errorMsg, th);
        }
    }

    public static Mail buildErrorMail(String from, List<String> to, String msg, String requestid) {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "error");
        Mail returnMail = new Mail(from, to, new ArrayList<>(), requestid, headers, msg);
        return returnMail;
    }

    private void addErrorMetrics(String type) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("type", type);
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.server.error", tagMap, 1.0);
    }

    private void addErrorMetrics(String type, String clientId, String cdc) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("type", type);
        tagMap.put("clientid", clientId);
        tagMap.put("cdc", cdc);
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.server.error", tagMap, 1.0);
    }
    private void addProcessMetrics(boolean isSuccess, String clientId, String cdc) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("success", isSuccess);
        tagMap.put("clientid", clientId);
        tagMap.put("cdc", cdc);
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.server.processcount", tagMap, 1.0);
    }
    private void addCostRangeMetrics(long cost, String clientId, String cdc) {
        Map<String, Object> tagMap = getBasicTagMap();
        tagMap.put("range", BabelMetricsHelper.getInstance().getRangeLabel(cost));
        tagMap.put("clientid", clientId);
        tagMap.put("cdc", cdc);
        BabelMetricsHelper.getInstance().addMetrics("fx", "babel.server.costrange", tagMap, 1.0);
    }

    private Map<String, Object> getBasicTagMap() {
        Map<String, Object> tags = new HashMap<>();
        tags.put("serverid", id);
        tags.put("service", meta.getName());
        tags.put("impl", meta.getServerImpl());
        tags.put("delivery", meta.getDeliverMode());
        tags.put("call", meta.getCallMode());
        tags.put("sdc", meta.getOption("sdc"));
        return tags;
    }
}

