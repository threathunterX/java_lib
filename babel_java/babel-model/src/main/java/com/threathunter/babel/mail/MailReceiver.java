package com.threathunter.babel.mail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A receiver has one background thread to help receive its own mails.
 *
 * @author Wen Lu
 */
public interface MailReceiver {

    /**
     * Start the background worker and begin to receive the mails.
     */
    public void startReceiving();

    /**
     * Stop receiving the mails.
     */
    public void stopReceiving() throws IOException;

    /**
     * Get one mail from the receiver.
     *
     * @return one mail if there is one is the local storage, null if there isn't one.
     */
    public Mail getMail() throws MailException;

    /**
     * Get one mail from the receiver in a given timeout.
     *
     * @param timeout timeout value
     * @param unit timeout unit, see {@link TimeUnit}
     * @return one mail if there is one mail coming before the deadline, null if there isn't one when the timer expires.
     */
    public Mail getMail(long timeout, TimeUnit unit) throws MailException;

    /**
     * Get all the mails in the local storage.
     *
     * @return all the available mails.
     */
    public List<Mail> drainMail();
}
