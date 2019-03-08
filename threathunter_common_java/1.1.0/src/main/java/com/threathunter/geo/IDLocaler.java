package com.threathunter.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by daisy on 16-1-18.
 */
public class IDLocaler {
    private static final String REGEX_ID = "^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$";

    private static int[] Wi = new int[] {
            7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2
    };
    private static char[] Ti = new char[] {
            '1', '0', 'x', '9', '8', '7', '6', '5', '4', '3', '2'
    };

    private static Map<String, String> idProvinceCityMap;

    public static void loadResource(String id_source) {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(id_source);
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            idProvinceCityMap = new HashMap<>();

            String line = bf.readLine();
            while (line != null && !line.trim().equals("")) {
                String[] lines = line.split(" ");
                idProvinceCityMap.put(lines[0], lines[1]);

                line = bf.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getGeo(String id) {
        if (isValidId(id)) {
            return getValidIdGeo(id);
        }
        return null;
    }

    private static boolean isValidId(String id) {
        if(Pattern.matches(REGEX_ID, id)) {
            if (id.length() == 15) {
                return true;
            }
            if (id.length() == 18) {
                int sum = 0;
                for (int i = 0; i < 17; i++) {
                    sum += Character.getNumericValue(id.charAt(i)) * Wi[i];
                }
                if (Ti[sum%11] == Character.toLowerCase(id.charAt(17))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getValidIdGeo(String id) {
        String result = idProvinceCityMap.get(id.substring(0, 6));
        if (result == null) {
            result = idProvinceCityMap.get(id.substring(0,2));
        }
        return result;
    }
}
