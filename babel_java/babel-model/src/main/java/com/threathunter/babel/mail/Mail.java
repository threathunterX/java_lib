package com.threathunter.babel.mail;

import java.nio.charset.Charset;
import java.util.*;

import static com.threathunter.common.Utility.argumentNotEmpty;

/**
 * Carrier of data transferred among distributed components.
 *
 * <p>The mail contains 3 types of information
 * <ul>
 *     <li>parties participated in the communication, including sender, receiver, and the reply </li>
 *     <li>meta data about the mail, these data is stored in a map, applications can define the headers themselves</li>
 *     <li>the body is the payload of the mail, it's in the form of the {@link byte[]}.
 *     text/string</li>
 * </ul>
 *
 * created by www.threathunter.cn
 */
public class Mail {

    public static final Charset BODY_CHARSET = Charset.forName("UTF-8");

    private String from;
    private List<String> to;
    private List<String> reply;
    private Map<String, String> headers;
    private byte[] body;
    private String requestid;

    private Mail() {
        // for json
    }

    /**
     * Mail Constructor.
     *
     * @param from the source of the mail.
     * @param to the destinations of the mail.
     * @param reply where the response mail should be sent to
     * @param headers the meta data of the mail, defined by the applications
     * @param body the binary body of the mail
     */
    public Mail(String from, List<String> to, List<String> reply, String requestid, Map<String, String> headers, byte[] body) {
        setFrom(from);
        setTo(to);
        setReply(reply);
        setHeaders(headers);
        setBody(body);
        setRequestid(requestid);
    }

    /**
     * Mail Constructor.
     *
     * @param from the source of the mail.
     * @param to the destinations of the mail.
     * @param reply where the response mail should be sent to
     * @param headers the meta data of the mail, defined by the applications
     * @param body the binary body of the mail
     */
    public Mail(String from, List<String> to, List<String> reply, String requestid, Map<String, String> headers, String body) {
        this(from, to, reply, requestid, headers, body.getBytes(BODY_CHARSET));
    }

    /**
     * Mail Constructor.
     *
     * <p>The mail is only sent to one destination, and the reply destination is
     * just the source.
     *
     * @param from the source of the mail.
     * @param to the destination of the mail.
     * @param body the binary body of the mail
     */
    public Mail(String from, String to, String requestid, String body) {
        this(from, Arrays.asList(to), Arrays.asList(from)/*reply to the sender*/, requestid, new HashMap<String, String>(), body);
    }

    public void setFrom(String from) {
        argumentNotEmpty(from, "from is null or empty");
        this.from = from;
    }

    public void setTo(List<String> to) {
        argumentNotEmpty(to, "to is null or empty");
        this.to = to;
    }

    public void setReply(List<String> reply) {
        // reply could be empty
        if (reply == null) {
            reply = new ArrayList<String>();
        }
        this.reply = reply;
    }

    public void setBody(byte[] body) {
        if (body == null) {
            body = new byte[0];
        }
        this.body = body;
    }

    public void setHeaders(Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        this.headers = headers;
    }

    public void setHeader(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
    }

    /**
     * The sender of the mail.
     *
     * @return
     */
    public String getFrom() {
        return from;
    }

    /**
     * The receivers of the mail
     *
     * @return
     */
    public List<String> getTo() {
        return to;
    }

    /**
     * Who should be the receiver if the receiver want to reply the mail.
     *
     * @return
     */
    public List<String> getReply() {
        return reply;
    }

    /**
     * Containing meta data about the mail.
     *
     * <p>In special scenarios, the headers can contain information like "type", "msgid", "compressing"
     * for more advanced functionalities.
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get a specific header.
     *
     * @param header header name
     * @return header value
     */
    public String getHeader(String header) {
        return this.headers.get(header);
    }

    /**
     * Get a specific header.
     *
     * @param header header name
     * @param defaultValue the default header value if the header is null
     * @return header value
     */
    public String getHeader(String header, String defaultValue) {
        String result = this.headers.get(header);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * The payload of the mail, you can encode the binary data with encodeing ISO-8859-1
     * @return
     */
    public byte[] getBody() {
        return body;
    }

    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mail mail = (Mail) o;

        if (!from.equals(mail.from)) return false;
        if (!to.equals(mail.to)) return false;
        if (reply != null ? !reply.equals(mail.reply) : mail.reply != null) return false;
        if (!headers.equals(mail.headers)) return false;
        return Arrays.equals(body, mail.body);

    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + (reply != null ? reply.hashCode() : 0);
        result = 31 * result + headers.hashCode();
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "from='" + from + '\'' +
                ", to=" + to +
                ", reply=" + reply +
                ", requestid=" + requestid +
                ", headers=" + headers +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
