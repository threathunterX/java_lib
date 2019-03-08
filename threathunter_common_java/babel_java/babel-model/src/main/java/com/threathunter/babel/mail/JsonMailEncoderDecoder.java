package com.threathunter.babel.mail;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.threathunter.common.Utility.isEmptyStr;

/**
 * Encode/Decoder that convert mail into/from json.
 * <p>>
 * Json is the default encoding for mail.
 *
 * @author Wen Lu
 */
public class JsonMailEncoderDecoder implements MailEncoder, MailDecoder {

    private static final Logger logger = LoggerFactory.getLogger(JsonMailEncoderDecoder.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String encode(Mail m) {
        try {
            return mapper.writer().writeValueAsString(new MailInJson(m));
        } catch (IOException e) {
            logger.error("data:fail to convert mail: " + m.toString(), e);
            return "";
        }
    }

    @Override
    public Mail decode(String str) {
        if (isEmptyStr(str)) {
            logger.warn("data:hey, null string cannot be converted to mail");
            return null;
        }

        try {
            MailInJson mij = mapper.reader(MailInJson.class).readValue(str);
            return mij.toMail();
        } catch (IOException e) {
            logger.error("data:fail to convert to the mail from a string: " + str, e);
            return null;
        }
    }

    /**
     * In order to communicate with other platforms and programming languages, the body should be converted to Base64
     * encoding in the json representation.
     */
    static class MailInJson {

        private String from;
        private List<String> to;
        private List<String> reply;
        private Map<String, String> headers;
        private String body;
        private String requestid;

        MailInJson() {
            // for json
        }

        MailInJson(Mail m) {
            this.from = m.getFrom();
            this.to = m.getTo();
            this.reply = m.getReply();
            this.headers = m.getHeaders();
            this.body = new BASE64Encoder().encode(m.getBody());
            this.requestid = m.getRequestid();
        }

        Mail toMail() {
            byte[] byteBody = null;
            try {
                byteBody = new BASE64Decoder().decodeBuffer(body);
            } catch (IOException e) {
                logger.error("data:fail to decode {}", body);
                return null;
            }
            return new Mail(from, to, reply, requestid, headers, byteBody);
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public List<String> getTo() {
            return to;
        }

        public void setTo(List<String> to) {
            this.to = to;
        }

        public List<String> getReply() {
            return reply;
        }

        public void setReply(List<String> reply) {
            this.reply = reply;
        }

        public String getRequestid(){
            return requestid;
        }

        public void setRequestid(String requestid) {
            this.requestid = requestid;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
