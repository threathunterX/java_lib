package com.threathunter.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Wen Lu
 */
public class Compressing {

    private static final Logger logger = LoggerFactory.getLogger(Compressing.class);

    protected final static int COMPRESSION_LEVEL = 5;//Deflater.DEFAULT_COMPRESSION;
    protected final static boolean NO_WRAP = false;


    public static byte[] compress(byte[] uncompressed) {
        Deflater deflater = new Deflater(COMPRESSION_LEVEL, NO_WRAP);
        deflater.reset();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DeflaterOutputStream out = new DeflaterOutputStream(bos, deflater, 4000);
        try {
            out.write(uncompressed);
            out.close();
        } catch (IOException e) {
            logger.error("data:error during compressing", e);
            return new byte[0];
        }

        return bos.toByteArray();
    }

    public static byte[] uncompress(byte[] compressed) {

        Inflater inflater = new Inflater(NO_WRAP);
        inflater.reset();

        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(compressed), inflater);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4000];
        int count = 0;
        try {
            while ((count = in.read(buffer, 0, 4000)) > 0) {
                bos.write(buffer, 0, count);
            }
        } catch (IOException e) {
            logger.error("data:fail to uncompress data", e);
            return new byte[0];
        }

        return bos.toByteArray();
    }
}
