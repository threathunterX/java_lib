package com.threathunter.babel.rpc;

import com.threathunter.babel.mail.Mail;
import com.threathunter.common.Compressing;
import com.threathunter.model.Event;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.threathunter.common.Utility.isEmptyStr;

/**
 * Utilities help rpc on mail.
 *
 * created by www.threathunter.cn
 */
public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper(); // used for json operation

    public static List<Event> extractEventsFromMail(Mail m) throws RemoteException {
        List<Event> result = new ArrayList<Event>();

        if (m == null) {
            return result;
        }

        String type = m.getHeader("content-type");
        byte[] body = m.getBody();
        String compressing = m.getHeader("compressing");

        try {
            if ("error".equals(type)) {
                throw new RemoteException(new String(body));
            }

            if (!isEmptyStr(compressing)) {
                if ("gzip".equalsIgnoreCase(compressing)) {
                    body = Compressing.uncompress(body);
                } else {
                    logger.error("data:rpc:the compressing " + compressing + " is not supported yet");
                    throw new RemoteException("unsuppored compressing method");
                }
            }
            if ("event".equalsIgnoreCase(type)) {
                // body is one event
                String s = new String(body);
                Event e = mapper.reader(Event.class).readValue(body);
                result.add(e);
            } else if ("event[]".equalsIgnoreCase(type)) {
                // body is a batch of event
                List<Event> eventlist = mapper.reader(new TypeReference<List<Event>>() {}).readValue(body);
                result.addAll(eventlist);
            } else {
                logger.error("data:rpc:this body type ({}) is not supported yet", type);
                throw new RemoteException("unsupported type");
            }
        } catch(RemoteException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new RemoteException("fail to extract events", ex);
        }

        return result;
    }

    public static void populateEventIntoMail(Mail m, Event e) throws RemoteException {
        if (m == null || e == null) {
            return;
        }

        Map<String, String> headers = m.getHeaders();
        if (headers == null) {
            headers = new HashMap<String, String>();
            m.setHeaders(headers);
        }

        try {
            byte[] body = mapper.writer().writeValueAsBytes(e);
            headers.put("content-type", "event");
            m.setBody(body);
        } catch (Exception ex) {
            String errorMsg = "fail to convert event " + e + "  to json";
            throw new RemoteException(errorMsg, ex);
        }

    }

    public static void populateEventListIntoMail(Mail m, List<Event> list) throws RemoteException {
        if (m == null) {
            return;
        }

        if (list == null) {
            list = new ArrayList<>();
        }

        Map<String, String> headers = m.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
            m.setHeaders(headers);
        }

        try {
            byte[] body = mapper.writer().writeValueAsBytes(list);
            if (body.length > 1000) {
                body = Compressing.compress(body);
                headers.put("compressing", "gzip");
            }
            m.setBody(body);
            headers.put("content-type", "event[]");
        } catch (IOException ex) {
            String errorMsg = "fail to convert event " + list + "  to json";
            throw new RemoteException(errorMsg, ex);
        }

    }
}
