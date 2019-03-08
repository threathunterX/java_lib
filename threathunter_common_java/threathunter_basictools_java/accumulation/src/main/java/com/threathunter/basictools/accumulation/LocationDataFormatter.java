package com.threathunter.basictools.accumulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by daisy on 2015/11/11.
 */
public class LocationDataFormatter {
    Map<String, Map<String, AtomicInteger>> allLineMap;
    TreeMap<String, Integer> sortedCityIn;
    TreeMap<String, Integer> sortedCityOut;
    TreeMap<String, Integer> sortedLines;

    public LocationDataFormatter(Map<String, Map<String, AtomicInteger>> allLineMap) {
        this.allLineMap = allLineMap;
    }

    public void parseMap() {
        Map<String, Integer> cityIn = new HashMap<>();
        Map<String, Integer> cityOut = new HashMap<>();
        Map<String, Integer> lineMap = new HashMap<>();

        for (Map.Entry<String, Map<String, AtomicInteger>> entry : allLineMap.entrySet()) {
            if (!cityIn.containsKey(entry.getKey())) {
                cityIn.put(entry.getKey(), 0);
            }
            int in = cityIn.get(entry.getKey());
            for (Map.Entry<String, AtomicInteger> fromCity : entry.getValue().entrySet()) {
                String line = String.join(":", entry.getKey(), fromCity.getKey());
                lineMap.put(line, fromCity.getValue().get());

                int out = fromCity.getValue().get();
                in += out;
                if (!cityOut.containsKey(fromCity.getKey())) {
                    cityOut.put(fromCity.getKey(), 0);
                }
                int fromCityOut = cityOut.get(fromCity.getKey());
                cityOut.put(fromCity.getKey(), fromCityOut + out);
            }
            cityIn.put(entry.getKey(), in);
        }

        this.sortedCityIn = new TreeMap<>(cityIn);
        this.sortedCityOut = new TreeMap<>(cityOut);
        this.sortedLines = new TreeMap<>(lineMap);
    }

    public JsonArray getTopCityIn() {
        JsonArray array = new JsonArray();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedCityIn.entrySet()) {
            if (count >= 10) {
                break;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", entry.getKey());
            jsonObject.addProperty("num", entry.getValue());
            jsonObject.addProperty("singleNum", entry.getValue());
            jsonObject.addProperty("per", 0.0301);
            jsonObject.addProperty("floatFlag", 0);
            array.add(jsonObject);

            count++;
        }
        return array;
    }

    public JsonArray getTopCityOut() {
        JsonArray array = new JsonArray();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedCityOut.entrySet()) {
            if (count >= 10) {
                break;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", entry.getKey());
            jsonObject.addProperty("num", entry.getValue());
            jsonObject.addProperty("singleNum", entry.getValue());
            jsonObject.addProperty("per", 0.0301);
            jsonObject.addProperty("floatFlag", 0);
            array.add(jsonObject);

            count++;
        }
        return array;
    }

    public JsonArray getTopLine() {
        JsonArray array = new JsonArray();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedLines.entrySet()) {
            if (count >= 10) {
                break;
            }
            JsonObject jsonObject = new JsonObject();
            String[] cities = entry.getKey().split(":");
            jsonObject.addProperty("start", cities[0]);
            jsonObject.addProperty("end", cities[1]);
            jsonObject.addProperty("num", entry.getValue());
            jsonObject.addProperty("singleNum", entry.getValue());
            jsonObject.addProperty("per", 0.0301);
            jsonObject.addProperty("floatFlag", 0);
            array.add(jsonObject);

            count++;
        }
        return array;
    }

    public JsonArray getAllLines() {
        JsonArray array = new JsonArray();
        for (Map.Entry<String, Integer> entry : sortedLines.entrySet()) {
            JsonObject jsonObject = new JsonObject();
            String[] cities = entry.getKey().split(":");
            jsonObject.addProperty("start", cities[0]);
            jsonObject.addProperty("end", cities[1]);
            jsonObject.addProperty("num", entry.getValue());
            jsonObject.addProperty("singleNum", entry.getValue());
            array.add(jsonObject);
        }
        return array;
    }

    public class CityInOutEntity {
        String name;
        int num;
        int singleNum;
        double per;
        double floatFlag;

        public CityInOutEntity(String city, int value) {
            this.name = city;
            this.num = value;
            this.singleNum = value;
            this.per = 0.0903;
            this.floatFlag = 0;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getSingleNum() {
            return singleNum;
        }

        public void setSingleNum(int singleNum) {
            this.singleNum = singleNum;
        }

        public double getPer() {
            return per;
        }

        public void setPer(double per) {
            this.per = per;
        }

        public double getFloatFlag() {
            return floatFlag;
        }

        public void setFloatFlag(double floatFlag) {
            this.floatFlag = floatFlag;
        }
    }

    public class TopLineEntity {
        String start;
        String end;
        int num;
        int singleNum;
        double per;
        double floatFlag;

        public TopLineEntity(String key, int value) {
            String[] startEnd = key.split(":");
            this.start = startEnd[0];
            this.end = startEnd[1];
            this.num = value;
            this.singleNum = value;
            this.per = 0.0078;
            this.floatFlag = 0;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getSingleNum() {
            return singleNum;
        }

        public void setSingleNum(int singleNum) {
            this.singleNum = singleNum;
        }

        public double getPer() {
            return per;
        }

        public void setPer(double per) {
            this.per = per;
        }

        public double getFloatFlag() {
            return floatFlag;
        }

        public void setFloatFlag(double floatFlag) {
            this.floatFlag = floatFlag;
        }
    }

    public class AllLineEntity {
        String start;
        String end;
        int num;
        int singleNum;

        public AllLineEntity(String key, int value) {
            String[] startEnd = key.split(":");
            this.start = startEnd[0];
            this.end = startEnd[1];
            this.num = value;
            this.singleNum = value;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getSingleNum() {
            return singleNum;
        }

        public void setSingleNum(int singleNum) {
            this.singleNum = singleNum;
        }
    }
}
