package com.threathunter.babel.mail;

/**
 * A sender helps send a mail.
 *
 * created by www.threathunter.cn
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
