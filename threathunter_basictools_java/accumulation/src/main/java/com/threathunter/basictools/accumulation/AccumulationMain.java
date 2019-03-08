package com.threathunter.basictools.accumulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.elasticsearch.common.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by daisy on 2015/11/11.
 */
public class AccumulationMain {
    static volatile long locationTopoStartTimeInSec;
    static long locationTopoPeriodInSec;
    static String outputPath;

    static HistoryAccumulationHelper historyAccumulationHelper;
    static LocationDataGetter getter;
    public static void main(String[] args) {
        ConfigHelper.getInstance().setConfig(args[0]);

        locationTopoStartTimeInSec = ConfigHelper.getInstance().getLocationTopoStartTimestamp();
        locationTopoPeriodInSec = ConfigHelper.getInstance().getLocationTopoPeriod();
        outputPath = ConfigHelper.getInstance().getJsonUrl();

        getter = new LocationDataGetter(ConfigHelper.getInstance().getESIp(), ConfigHelper.getInstance().getESPort());

        historyAccumulationHelper = new HistoryAccumulationHelper();
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(3);

        long accumulationPeriodInSec = ConfigHelper.getInstance().getAccumulationPeriod();
        long locationUpdatePeriodInSec = ConfigHelper.getInstance().getLocationTopoUpdatePeriod();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                getAccumulateJson();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, accumulationPeriodInSec, TimeUnit.SECONDS);
        System.out.println(accumulationPeriodInSec);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                getLocationTopoJson();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, locationUpdatePeriodInSec, TimeUnit.SECONDS);
        System.out.println(locationUpdatePeriodInSec);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                historyAccumulationHelper.everyDayMysqlTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, getDelaysSec(8, 5, 0), 60 * 60 * 24, TimeUnit.SECONDS);

        while (true) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
    }

    static long getDelaysSec(int atHour, int atMinute, int atSecond) {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTime = zonedNow.withHour(atHour).withMinute(atMinute).withSecond(atSecond);
        if (zonedNow.compareTo(zonedNextTime) > 0) {
            zonedNextTime = zonedNextTime.plusDays(1);
        }
        Duration duration = Duration.between(zonedNow, zonedNextTime);
        return duration.getSeconds();
    }

    static void getAccumulateJson() {
        System.out.println(String.format("[%s]get total and risk", DateTime.now().toString()));
        HistoryAccumulationHelper.HistoryCount total = historyAccumulationHelper.getHistoryTotal();
        HistoryAccumulationHelper.HistoryCount risk = historyAccumulationHelper.getHistoryRisk();
        try {
            File temp = File.createTempFile("api.json", ".tmp", new File(outputPath));
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            JsonObject json = new JsonObject();
            json.addProperty("total", total.count);
            json.addProperty("risk", risk.count);
            bw.write(json.toString());
            bw.flush();
            bw.close();

            replaceFileContent(temp, "api.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void getLocationTopoJson() {
        System.out.println(String.format("[%s]get map", DateTime.now().toString()));
        LocationDataFormatter formatter = new LocationDataFormatter(getter.getLocationTopoJson(ConfigHelper.getInstance().getIndexPrefix(), ConfigHelper.getInstance().getTypes(), locationTopoStartTimeInSec, locationTopoPeriodInSec));
        locationTopoStartTimeInSec += locationTopoPeriodInSec;
        formatter.parseMap();
        try {
            File temp = File.createTempFile("map.json", ".tmp", new File(outputPath));
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("topCityIn", formatter.getTopCityIn());
            jsonObject.add("topCityOut", formatter.getTopCityOut());
            jsonObject.add("topLine", formatter.getTopLine());
            jsonObject.add("allLine", formatter.getAllLines());
            gson.toJson(jsonObject, bw);
            bw.flush();

            bw.close();

            replaceFileContent(temp, "map.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void replaceFileContent(File tempFile, String origName) {
        try {
            File origFile = new File(outputPath + origName);
            if (!origFile.exists()) {
                origFile.createNewFile();
            }
            Files.move(tempFile.toPath(), origFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
