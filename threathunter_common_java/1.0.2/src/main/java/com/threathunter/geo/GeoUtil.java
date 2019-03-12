package com.threathunter.geo;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class GeoUtil {
    private static final String[] GEO_LAN = new String[] {
            "内网",
            "内网",
            "内网"
    };
    private static final String[] GEO_UNKNOWN = new String[] {
            "未知",
            "未知",
            "未知"
    };
    private static Map<String, Object> provinces;

    static {
        Yaml yaml = new Yaml();
        InputStream is;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cities.yml");
            provinces = (Map<String, Object>)yaml.load(is);
            IPLocaler.loadResource("threathunter_ip.dat");
            IDLocaler.loadResource("id.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIPLocation(String ip) {
        return getLocation(getIPGeo(ip));
    }

    public static String getCNIPProvince(String ip) {
        return getCNGeoProvince(getIPGeo(ip));
    }

    public static String getCNIPCity(String ip) {
        return getGeoCity(getIPGeo(ip));
    }

    public static String getPhoneLocation(String number) {
       return getLocation(getPhoneGeo(number));
    }

    public static String getCNPhoneProvince(String number) {
        return getCNGeoProvince(getPhoneGeo(number));
    }

    public static String getCNPhoneCity(String number) {
        return getGeoCity(getPhoneGeo(number));
    }

    public static String getIDLocation(String id) {
        return getLocation(getIDGeo(id));
    }
    public static String getCNIDProvince(String id) {
        return getCNGeoProvince(getIDGeo(id));
    }
    public static String getCNIDCity(String id) {
        return getGeoCity(getIDGeo(id));
    }

    private static String getLocation(String[] geo) {
        return String.join(" ", geo);
    }

    private static String getCNGeoProvince(String[] geo) {
        return geo[1];
    }

    private static String getGeoCity(String[] geo) {
        if (geo.length < 3) {
            return geo[geo.length - 1];
        }
        return geo[2];
    }

    public static String[] getIPGeo(String ip) {
        try {
            if (IPUtil.isLAN(ip)) {
                return GEO_LAN;
            }
        } catch (Exception e) {
            return null;
        }
        String geo = IPLocaler.find(ip);
        if (geo == null) {
            return GEO_UNKNOWN;
        }

        String[] geoList = geo.split(",");
        if (geoList.length <= 0) {
            return GEO_UNKNOWN;
        }
        if (!geoList[0].equals("中国")) {
            return new String[] { geoList[0], geoList[0], geoList[0] };
        }
        return geoList;
    }
    public static String[] getPhoneGeo(String number) {
        return getGeoJsonObject(PhoneNumberLocaler.getGeo(number));
    }
    public static String[] getIDGeo(String id) {
        return getGeoJsonObject(IDLocaler.getGeo(id));
    }

    private static String[] getGeoJsonObject(String unformatted) {
        if (unformatted == null) {
            return GEO_UNKNOWN;
        }

        unformatted = unformatted.replaceAll("\\s*", "");
        int countryEndIndex = unformatted.indexOf("国");
        if (countryEndIndex < 1 || unformatted.length() <= countryEndIndex + 2) {
            // invalid
            return GEO_UNKNOWN;
        }
        String provincePrefix = unformatted.substring(countryEndIndex + 1, countryEndIndex + 3);
        Map<String, Object> provinceValue = (Map<String, Object>)provinces.get(provincePrefix);
        if (provinceValue == null) {
            // invalid
            String foreign = unformatted.substring(0, countryEndIndex + 1);
            return new String[]{ foreign, foreign, foreign };
        }

        String province = (String)provinceValue.get("名称");
        Map<String, Object> cityMap = (Map<String, Object>)provinceValue.get("城市");
        if (cityMap == null) {
            // only province
            return new String[]{"中国", province, province};
        }

        String city = getCity(2, unformatted, cityMap);
        if (city == null) {
            return new String[]{"中国", province, province};
        }

        return new String[]{"中国", province, city};
    }

    private static String getCity(int matchCount, String unformatted, Map<String, Object> searchMap) {
        int index = 2;
        if (unformatted.length() < 4) {
            return null;
        }
        while (index < unformatted.length() - 1) {
            Map<String, Object> cityMap = (Map<String, Object>)searchMap.get(unformatted.substring(index, index + matchCount));
            if (cityMap != null) {
                return (String)cityMap.get("名称");
            }
            index++;
        }
        return null;
    }
}
