package com.threathunter.metricsagent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * created by www.threathunter.cn
 */
public class Car {
    private static Random random = new Random();
    private static String[] countries = new String[] {"China", "Germany", "America"};
    private static String[] types = new String[] {"SUV", "MPV", "MINI"};
    private static String[] levels = new String[] {"first-class", "second-class", "third-class"};
    private static Double[][] leveledPrices = new Double[][] {{10.0, 15.0, 18.0}, {20.0, 30.0, 50.0}, {80.0, 100.0, 200.0}};

    String country;
    String type;
    String level;
    Double price;
    Map<String, Object> tags;
    Map<String, Object> getTags() {
        return tags;
    }
    Double getPrice() {
        return price;
    }

    void fillSelf() {
        country = countries[random.nextInt(10) % 3];
        type = types[random.nextInt(10) % 3];
        int levelIndex = random.nextInt(10) % 3;
        level = levels[levelIndex];
        price = leveledPrices[levelIndex][random.nextInt(10) % 3];
        tags = new HashMap<>();
        tags.put("country", country);
        tags.put("level", level);
        tags.put("type", type);
    }
}
