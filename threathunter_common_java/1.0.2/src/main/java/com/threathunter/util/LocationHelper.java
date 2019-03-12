package com.threathunter.util;

import com.threathunter.geo.GeoUtil;

/**
 * created by www.threathunter.cn
 */
public class LocationHelper {

    public static String getLocation(String param, String type) {
        try {
            if (param.contains(".")) {
                return getTypedIPLocation(param, type);
            }
            if (param.length() == 15 || param.length() == 18) {
                return getTypedIDLocation(param, type);
            }
            if (param.length() == 11 || param.startsWith("86") || param.startsWith("+86")) {
                return getTypedPhoneLocation(param, type);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String getTypedIPLocation(String ip, String type) {
        if (type.equals("province")) {
            return GeoUtil.getCNIPProvince(ip);
        }
        if (type.equals("city")) {
            return GeoUtil.getCNIPCity(ip);
        }
        return GeoUtil.getIPLocation(ip);
    }

    private static String getTypedIDLocation(String id, String type) {
        if (type.equals("province")) {
            return GeoUtil.getCNIDProvince(id);
        }
        if (type.equals("city")) {
            return GeoUtil.getCNIDCity(id);
        }
        return GeoUtil.getIDLocation(id);
    }

    private static String getTypedPhoneLocation(String phone, String type) {
        if (type.equals("province")) {
            return GeoUtil.getCNPhoneProvince(phone);
        }
        if (type.equals("city")) {
            return GeoUtil.getCNPhoneCity(phone);
        }
        return GeoUtil.getPhoneLocation(phone);
    }
}
