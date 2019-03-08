package com.threathunter.logging.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;

/**
 * Created by daisy on 16/12/30.
 */
public class AppenderManager {
    private static final AppenderManager INSTANCE = new AppenderManager();

    public static AppenderManager getInstance() {
        return INSTANCE;
    }

    /**
     * Default logger is root
     */
    public void initSentryFromConfig() {
        this.initSentryFromConfig(Logger.ROOT_LOGGER_NAME);
    }

    public void initSentryFromConfig(String loggerName) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(loggerName);

        Appender appender = SentryAppenderCreator.createAppender(context);
        appender.start();
        logger.addAppender(appender);
    }
}
