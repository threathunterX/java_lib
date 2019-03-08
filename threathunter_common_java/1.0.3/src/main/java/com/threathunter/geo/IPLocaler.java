package com.threathunter.geo;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Daisy on 2015/8/3.
 */
public class IPLocaler {
    public static String randomIp() {
        Random r = new Random();
        StringBuffer str = new StringBuffer();
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(r.nextInt(1000000) % 255);
        str.append(".");
        str.append(0);

        return str.toString();
    }

    private static int ipNumber;
    private static long[] ipArray;
    private static String[] ipLocalStringArray;
    private static int offset;
    private static int[] index;
    private static ByteBuffer dataBuffer;
    private static ReentrantLock lock = new ReentrantLock();


    public static void loadFile(String fileName) throws IOException {
        InputStream is = new FileInputStream(new File(fileName));
        loadStream(is);
        is.close();
    }

    public static void loadResource(String resourceName) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        loadStream(is);
        is.close();
    }

    private static void loadStream(InputStream is) throws IOException {
        if (is == null) {
            return;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = is.read(buffer, 0, 4096)) >= 0) {
            bos.write(buffer, 0, read);
        }
        dataBuffer = ByteBuffer.wrap(bos.toByteArray());
        load();
    }

    public static String find(String ip) {
        try {
            ip = ip.trim();
            int ip_prefix_value = new Integer(ip.substring(0, ip.indexOf(".")));
            long ip2long_value = ip2long(ip);
            int start = index[ip_prefix_value];
            int end = index[ip_prefix_value + 1];

            while (start < end) {
                int mid = (start + end) / 2;
                long mid_val = ipArray[mid];

                if (mid_val < ip2long_value) {
                    start = mid + 1;
                } else {
                    end = mid;
                }
            }
            if (start >= ipNumber) {
                return null;
            }
            return ipLocalStringArray[start];
        } catch (Exception e) {
            throw new RuntimeException("error with ip: " + ip, e);
        }
    }

    private static void load() {
        lock.lock();
        try {
            dataBuffer.position(0);
            offset = dataBuffer.getInt();
            offset -= 1024;

            index = new int[257];
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < 256; i++) {
                int start = i * 4 + 4;
                index[i] = dataBuffer.getInt(start);
            }
            index[256] = (offset - 1028) /8;

            ipNumber = (offset - 1028) /8;
            ipArray = new long[ipNumber];
            ipLocalStringArray = new String[ipNumber];

            byte b = 0;
            dataBuffer.order(ByteOrder.BIG_ENDIAN);
            for (int i = 0; i < ipNumber; ++i) {
                int pos = 1028 + 8 *i;
                long ip_int = int2long(dataBuffer.getInt(pos));
                long _offset = bytesToLong(b, dataBuffer.get(pos + 6), dataBuffer.get(pos + 5), dataBuffer.get(pos + 4));
                int length = 0xFF & dataBuffer.get(pos + 7);

                ipArray[i] = ip_int;
                byte[] value = new byte[length];
                dataBuffer.position((int)_offset + offset);
                dataBuffer.get(value, 0, length);

                String result = new String(value, Charset.forName("UTF-8")).replaceAll("\\s+", ",");
//                String result = new String(value);
                ipLocalStringArray[i] = result;
            }

        } finally {
            lock.unlock();
        }
    }

    private static long bytesToLong(byte a, byte b, byte c, byte d) {
        return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
    }

    private static int str2Ip(String ip)  {
        String[] ss = ip.split("\\.");
        int a, b, c, d;
        a = Integer.parseInt(ss[0]);
        b = Integer.parseInt(ss[1]);
        c = Integer.parseInt(ss[2]);
        d = Integer.parseInt(ss[3]);
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    private static long ip2long(String ip)  {
        return int2long(str2Ip(ip));
    }

    private static long int2long(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
}
