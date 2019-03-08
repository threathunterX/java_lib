package com.threathunter.geo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daisy on 2015/8/5.
 */
public class FormatLatitudeLongitude {
    private List<String> toAddRecord;
    private List<String> toAddError;
    BufferedWriter bw;
    BufferedWriter bw2;

    @Before
    public void setUp() throws FileNotFoundException, UnsupportedEncodingException {
//        GeoUtil.load("C:\\Users\\daisy\\Desktop\\ipphone\\cities.yml", "C:\\Users\\daisy\\Desktop\\threathunter_ip.dat");
        toAddRecord = new ArrayList<>();
        toAddError = new ArrayList<>();
        String outPath = "C:\\Users\\daisy\\Desktop\\toAddRecord.txt";
        String errorPath = "C:\\Users\\daisy\\Desktop\\toAddError.txt";
        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8"));
        bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errorPath), "UTF-8"));
    }

    @After
    public void tearDown() {
        System.out.println("flushing data...");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("finish flushing data!");
    }

    @Test
    public void format() throws IOException {
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\daisy\\Desktop\\cities_location.txt"), "UTF-8"));
//        String line = br.readLine();
//        while (line != null) {
//            String[] list = line.split(",");
//            if (list.length != 3) {
//                toAddError.add(line);
//            }
//            String str = list[2].replaceAll("\\s+", "");
//            String[] geo = GeoUtil.getGeoJsonObject(str.substring(2, str.length()));
//            String city = "";
//            if (geo != null) {
//                for (String s : geo) {
//                    city += s;
//                }
//                toAddRecord.add(String.format("%s,%s,%s", city, list[0].trim(), list[1].trim()));
//            } else {
//                toAddError.add(line + " geo: " + Arrays.toString(geo));
//            }
//            line = br.readLine();
//        }
    }
    @Test
    public void formatWithoutProvince() throws IOException {
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\daisy\\Desktop\\cities_location.txt"), "UTF-8"));
//        String line = br.readLine();
//        while (line != null) {
//            String[] list = line.split(",");
//            if (list.length != 3) {
//                toAddError.add(line);
//            }
//            String str = list[2].replaceAll("\\s+", "");
//            String city = GeoUtil.getGeoCity(GeoUtil.getGeoJsonObject(str.substring(2, str.length())));;
//            if (city != null) {
//                toAddRecord.add(String.format("%s,%s,%s", city, list[0].trim(), list[1].trim()));
//            } else {
//                toAddError.add(line + " city: null");
//            }
//            line = br.readLine();
//        }
    }
}
