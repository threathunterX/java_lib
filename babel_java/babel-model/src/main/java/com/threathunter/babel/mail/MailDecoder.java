package com.threathunter.babel.mail;

/**
 * Decode the mail from a String.
 *
 * created by www.threathunter.cn
 */
public interface MailDecoder {

    /**
     * Convert the string into mail.
     *
     * @param str
     * @return mail from the str
     */
    public Mail decode(String str);
}
