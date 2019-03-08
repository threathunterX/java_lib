package com.threathunter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;

/**
 * Created by daisy on 16/10/31.
 */
public class ConfigUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static String[] getFileUrl(String... fileNames) {
        StringBuilder builder = new StringBuilder();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            for (String fileName : fileNames) {
                URL url = loader.getResource(fileName);
                String path;
                try {
                    path = url.getPath();
                } catch (Exception e) {
                    logger.error("config file does not exist: " + fileName, e);
                    continue;
                }
                builder.append("file:").append(path).append(",");
            }
        }

        if (builder.length() > 0) {
            try {
                return builder.substring(0, builder.length() - 1).split(",");
            } catch (Exception e) {
                logger.error("unable to add config files: " + Arrays.toString(fileNames), e);
            }
        }
        return null;
    }

}
