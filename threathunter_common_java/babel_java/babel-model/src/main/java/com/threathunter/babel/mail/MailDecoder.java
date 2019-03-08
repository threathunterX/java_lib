package com.threathunter.babel.mail;

/**
 * Decode the mail from a String.
 *
 * @author Wen Lu
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
