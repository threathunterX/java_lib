package com.threathunter.logging.logback;

import ch.qos.logback.core.Context;
import com.threathunter.config.CommonDynamicConfig;
import com.getsentry.raven.logback.SentryAppender;
import ch.qos.logback.core.Appender;

/**
 * created by www.threathunter.cn
 */
public class SentryAppenderCreator {
    public static Appender createAppender(Context context) {
        CommonDynamicConfig config = CommonDynamicConfig.getInstance();
        if (!config.containsKey("sentry_dsn")) {
            throw new RuntimeException("create appender error, missing sentry dsn");
        }
        SentryAppender appender = new SentryAppender();
        appender.setDsn(config.getString("sentry_dsn"));
        appender.setName(config.getString("sentry_appender_name", "SENTRY"));
        appender.setMinLevel(config.getString("sentry_min_level", "error"));
        appender.setRavenFactory(config.getString("sentry_factory", "com.getsentry.raven.DefaultRavenFactory"));
        if (config.containsKey("sentry_environment")) {
            appender.setEnvironment(config.getString("sentry_environment"));
        }
        if (config.containsKey("sentry_tags")) {
            appender.setTags(config.getString("tags"));
        }
        if (config.containsKey("sentry_extra_tags")) {
            appender.setExtraTags(config.getString("sentry_extra_tags"));
        }
        if (config.containsKey("server_name")) {
            appender.setServerName(config.getString("server_name"));
        }
        if (config.containsKey("release")) {
            appender.setRelease(config.getString("release"));
        }

        appender.setContext(context);

        return appender;
    }
}
