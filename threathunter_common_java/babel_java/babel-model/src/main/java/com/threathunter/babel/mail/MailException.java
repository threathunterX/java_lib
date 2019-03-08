package com.threathunter.babel.mail;

/**
 * Created by lw on 2015/4/28.
 */
public class MailException extends Exception {
    public MailException() {
        super();
    }

    public MailException(String message) {
        super(message);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailException(Throwable cause) {
        super(cause);
    }

    protected MailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
