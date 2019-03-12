package com.threathunter.babel.mail;

/**
 * Encode the mail into a String.
 *
 * created by www.threathunter.cn
 */
public interface MailEncoder {
    /**
     * Convert the mail into a String with UTF-8 encoding.
     *
     * @param m the mail
     * @return String with UTF-8 encoding(for the binary payload).
     */
    String encode(Mail m);
}
