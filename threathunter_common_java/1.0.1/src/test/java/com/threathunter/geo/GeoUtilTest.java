package com.threathunter.geo;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * created by www.threathunter.cn
 */
public class GeoUtilTest {

    @BeforeClass
    public static void setup() {
        IDLocaler.loadResource("id.txt");
    }

    @Test
    public void testLocationUtil() {
        String pl = GeoUtil.getPhoneLocation("+442083661177");
        String il = GeoUtil.getIPLocation("116.231.73.162");
        String idL = GeoUtil.getIDLocation("511623199207020025");
        System.out.println(pl);
        System.out.println(il);
        System.out.println(idL);
    }

    @Test
    public void testGeoUtilProvice() {
        String pgeo = GeoUtil.getCNPhoneProvince("13000000001");
        String igeo = GeoUtil.getCNIPProvince("1.0.3.255");
        String idGeo = GeoUtil.getCNIDProvince("511623199207020025");
        System.out.println(pgeo);
        System.out.println(igeo);
        System.out.println(idGeo);
    }

    @Test
    public void testGeoUtilCity() {
        String pgeo = GeoUtil.getCNPhoneCity("13000000001");
        String igeo = GeoUtil.getCNIPCity("1.0.3.255");
        String idGeo = GeoUtil.getCNIDCity("511623199207020025");
        System.out.println(pgeo);
        System.out.println(igeo);
        System.out.println(idGeo);
    }

    @Test
    public void testIP() {
        String city = GeoUtil.getCNIPCity("101.231.114.58");
        System.out.println(city);
        city = GeoUtil.getCNIPCity("58.67.199.255");
        System.out.println(city);
        String province = GeoUtil.getCNIPProvince("58.67.199.255");
        System.out.println(province);

        String ip = "203.190.249.0";
        city = GeoUtil.getCNIPCity(ip);
        System.out.println(city);

        ip = "59.49.145.132";
        city = GeoUtil.getCNIPCity(ip);
        System.out.println(city);

        ip = "121.63.83.33";
        city = GeoUtil.getCNIPCity(ip);
        System.out.println(city);

        ip = "103.255.228.0";
        city = GeoUtil.getCNIPCity(ip);
        System.out.println(city);

        ip = "203.79.228.16";
        city = GeoUtil.getCNIPCity(ip);
        System.out.println(city);

        ip = "1.36.0.0";
        city = GeoUtil.getCNIPCity(ip);
        System.out.println(city);

    }

    @Test
    public void testPhonePerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String mobile = PhoneNumberLocaler.randomPhone();
            try {
                GeoUtil.getPhoneLocation(mobile);
            } catch (Exception e) {
                System.out.println(mobile);
                break;
            }
        }
        System.out.println(System.currentTimeMillis() - start);
        // 10w+ per second
    }

    @Test
    public void testIPPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            GeoUtil.getIPLocation(IPLocaler.randomIp());
        }
        System.out.println(System.currentTimeMillis() - start);
        // 200+w per second
    }

    @Test
    public void testIDPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            GeoUtil.getIDLocation("511623199207020025");
        }
        System.out.println(System.currentTimeMillis() - start);
        // 30+w per second
    }

    @Test
    public void testLANIP() {
        String ip1 = "0.0.0.0";
//        String ip1 = "10.4.36.154";
        String ip2 = "172.18.4.1";
        String ip3 = "192.168.11.1";
        String ip4 = "59.49.145.132";
        System.out.println(GeoUtil.getIPLocation(ip1));
        System.out.println(GeoUtil.getCNIPProvince(ip1));
        System.out.println(GeoUtil.getCNIPCity(ip1));
        System.out.println(GeoUtil.getIPLocation(ip2));
        System.out.println(GeoUtil.getIPLocation(ip3));
        System.out.println(GeoUtil.getIPLocation(ip4));
        System.out.println(GeoUtil.getCNIPProvince(ip4));
        System.out.println(GeoUtil.getCNIPCity(ip4));
        System.out.println(IPLocaler.find(ip1));
    }
}
