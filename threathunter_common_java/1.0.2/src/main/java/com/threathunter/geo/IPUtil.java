package com.threathunter.geo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by daisy on 16/10/10.
 */
public class IPUtil {
    // 10.0.0.0/8
    private static final byte CATEGORY_1 = 0x0A;
    // 172.16.0.0/12
    private static final byte CATEGORY_2_1 = (byte) 0xAC;
    private static final byte CATEGORY_2_2 = (byte) 0x10;
    private static final byte CATEGORY_2_3 = (byte) 0x1F;
    // 192.168.0.0/16
    private static final byte CATEGORY_3_1 = (byte) 0xC0;
    private static final byte CATEGORY_3_2 = (byte) 0xA8;
    // 127.0.0.1/8
    private static final byte CATEGORY_4 = (byte) 0x7F;

    public static boolean isLAN(String ip) throws UnknownHostException {
        byte[] bIp = InetAddress.getByName(ip).getAddress();

        switch (bIp[0]) {
            case CATEGORY_1:
                return true;
            case CATEGORY_4:
                return true;
            case CATEGORY_2_1:
                if (bIp[1] >= CATEGORY_2_2 && bIp[1] <= CATEGORY_2_3) {
                    return true;
                }
            case CATEGORY_3_1:
                if (bIp[1] == CATEGORY_3_2) {
                    return true;
                }
            default:
                return false;
        }
    }
}
