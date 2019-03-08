package com.threathunter.babel.meta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Wen Lu
 */
public class ServiceMetaUtil {
    public static ServiceMeta getMetaFromResourceFile(String file) {
        try {
            String content = new String(getBytesFromResource(file));
            return ServiceMeta.from_json(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] getBytesFromResource(String file) throws IOException {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read = 0;
            while((read = input.read(buffer, 0, 1024)) >= 0) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }
}
