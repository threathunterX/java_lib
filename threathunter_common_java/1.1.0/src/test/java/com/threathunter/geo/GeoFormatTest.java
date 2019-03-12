package com.threathunter.geo;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * created by www.threathunter.cn
 */
public class GeoFormatTest {
    private List<String> toAddRecord;
    private List<String> ipShortInfo;
    private List<String> toAddError;
    BufferedWriter bw;
    BufferedWriter bw2;
    BufferedWriter bw3;
    long searchCount = 0;

//    @Before
//    public void testGeoLoad() throws FileNotFoundException {
//        GeoUtil.load("C:\\Users\\daisy\\Desktop\\ipphone\\cities.yml", "");
//    }

    @Test
    public void testFormat() {
        int count = 1;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
//            System.out.println(GeoUtil.getGeoJsonObject("内蒙古呼伦贝尔市"));
//            GeoUtil.getGeoJsonObject("内蒙古乌兰察");
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testAllPhonePrefix() throws IOException {
        Long sPhoneNumber = 13000000000l;
        Long ePhoneNumber = 18999990000l;

        startRecordComponents();
        while (sPhoneNumber <= ePhoneNumber) {
            String geoRaw = PhoneNumberLocaler.getGeo(sPhoneNumber.toString());
//            String geoRaw = PhoneNumberLocaler.getGeo("13000690000", "86");
            String geo = geoRaw;
            if (geoRaw.startsWith("中国")) {
                geo = geoRaw.substring(2, geoRaw.length());
            }
            if (geo == null || geo.length() <= 0) {
                toAddError.add("geo is null, phone: " + sPhoneNumber + " geo: " + geoRaw);
            } else {
//                try {
//                    String[] name = GeoUtil.getGeoJsonObject(geo);
//                    if (name == null) {
//                        toAddRecord.add(sPhoneNumber + ":" + geoRaw);
//                    }
//                    if (name.length >= 3) {
//                        toAddRecord.add(sPhoneNumber + ":" + Arrays.toString(name));
//                    }
//                } catch (Exception e) {
//                    toAddError.add(sPhoneNumber + ":" + geoRaw);
//                }
            }
            sPhoneNumber += 10000;
            checkToFlush();
        }
        stopRecordComponents();
    }

    private void checkToFlush() throws IOException {
        if (toAddRecord.size() >= 1000) {
            String[] records = toAddRecord.toArray(new String[toAddRecord.size()]);
            for (String record : records) {
                bw.write(record);
                bw.newLine();
            }
            bw.flush();
            toAddRecord.clear();
        }
        if (toAddError.size() >= 1000) {
            String[] records = toAddError.toArray(new String[toAddError.size()]);
            for (String record : records) {
                bw2.write(record);
                bw2.newLine();
            }
            bw2.flush();
            toAddError.clear();
        }
        if (ipShortInfo.size() >= 1000) {
            String[] records = ipShortInfo.toArray(new String[ipShortInfo.size()]);
            for (String record : records) {
                bw3.write(record);
                bw3.newLine();
            }
            bw3.flush();
            ipShortInfo.clear();
        }
    }

    private void startRecordComponents() throws FileNotFoundException, UnsupportedEncodingException {
        toAddRecord = new ArrayList<>();
        ipShortInfo = new ArrayList<>();
        toAddError = new ArrayList<>();
        String outPath = "toAddRecord.txt";
        String shortPath = "toCheckShort.txt";
        String errorPath = "toAddError.txt";
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8"));
        bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorPath), "UTF-8"));
        bw3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(shortPath), "UTF-8"));
    }

    private void stopRecordComponents() throws IOException {
        System.out.println("flushing rest data...");
        try {
            if (toAddRecord.size() > 0) {
                String[] records = toAddRecord.toArray(new String[toAddRecord.size()]);
                for (String record : records) {
                    bw.write(record);
                    bw.newLine();
                }
            }
            bw.flush();
            bw.close();
            if (toAddError.size() > 0) {
                String[] errors = toAddError.toArray(new String[toAddError.size()]);
                for (String error : errors) {
                    bw2.write(error);
                    bw2.newLine();
                }
            }
            bw2.flush();
            bw2.close();
            if (ipShortInfo.size() > 0) {
                String[] shorts = ipShortInfo.toArray(new String[ipShortInfo.size()]);
                for (String s : shorts) {
                    bw3.write(s);
                    bw3.newLine();
                }
            }
            bw3.flush();
            bw3.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("finish flushing data!");
    }

    @Test
    public void testAllIP() throws IOException {
        int a = 1;
        int b = 0;
        int c = 0;
        IPLocaler.loadResource("threathunter_ip.dat");
        startRecordComponents();

        while (a < 224) {
            // skip reserved location of class a
            if (a == 10) {
                a=11;
                continue;
            }
            if (a == 127) {
                a=128;
                continue;
            }

            // skip reserved location of class b
            if (a == 172) {
                if (b == 16) {
                    b = 32;
                    continue;
                }
            }
            if (a == 169) {
                if (b == 254) {
                    b = 255;
                    continue;
                }
            }

            // skip reserved location of class c
            if (a == 192) {
                if (b == 168) {
                    b = 169;
                    continue;
                }
            }

            // search the location
            String ip = String.format("%d.%d.%d.0", a, b, c);
            searchIpLocale(ip);

            c++;
            if (c == 256) {
                c = 0;
                b++;
            }
            if (b == 256) {
                b = 0;
                a++;
            }
            checkToFlush();
        }
        stopRecordComponents();
        // 1495257
        System.out.println(searchCount);
    }

    private void searchIpLocale(String ip) {
//        String[] list = null;
//        try {
//            list = IPLocaler.find(ip);
////            list = IPLocaler.find(ip);
//        } catch (Exception e) {
//            System.out.println(ip);
//        }
////            String[] list = IPLocaler.find("59.75.0.0");
//        if (list[0].equals("中国")) {
//            searchCount++;
//            if (list.length < 2) {
//                ipShortInfo.add(ip + ":" + list[0]);
//                return;
//            }
//            String geo = list[1];
//            for (int i = 2; i < list.length; i++) {
//                 geo += list[i];
//            }
//            try {
//                String[] name = GeoUtil.getGeoJsonObject(geo);
//                if (name == null) {
//                    if (list.length < 3) {
//                        ipShortInfo.add(ip + ":" + geo);
//                    } else {
//                        toAddError.add(ip + ":" + geo);
//                    }
//                } else if (name.length >= 3) {
//                    toAddRecord.add(ip + ":" + Arrays.toString(name) + "\t find: " + Arrays.toString(list));
//                }
//            } catch (Exception e) {
//                toAddError.add("[error]"+ip + ":" + geo);
//            }
//        }
    }
}
