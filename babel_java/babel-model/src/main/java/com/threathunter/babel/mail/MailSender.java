package com.threathunter.babel.mail;

/**
 * A sender helps send a mail.
 *
 * @author Wen Lu
 */
public interface MailSender {

    /**
     * Send one mail.
     *
     * @param m the mail to be sent.
     */
    public void sendMail(Mail m) throws MailException;

    void closeConnection();

}
