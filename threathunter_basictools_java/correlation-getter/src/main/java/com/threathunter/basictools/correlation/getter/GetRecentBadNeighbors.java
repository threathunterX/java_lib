package com.threathunter.basictools.correlation.getter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daisy on 2015/10/26.
 */
public class GetRecentBadNeighbors {
    public static void main(String[] args) throws IOException {
        String bad_neighbors = "C:\\Users\\daisy\\Desktop\\now\\network";
        String to_find = "C:\\Users\\daisy\\Desktop\\now\\random.txt";
        String output = "C:\\Users\\daisy\\Desktop\\now\\output";

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(bad_neighbors), "UTF-8"));

        Map<String, String> firstLayerNeighborCountMap = new HashMap<>();

        String line = br.readLine();
        try {
            while (line != null) {
                String[] neighborList = line.split("\\|");
                if (neighborList.length < 2) {
                    line = br.readLine();
                    continue;
                }
                if (neighborList[1] == null || neighborList[1].trim().length() <= 0) {
                    line = br.readLine();
                    continue;
                }
                firstLayerNeighborCountMap.put(neighborList[0], neighborList[1]);
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("line: " + line);
            e.printStackTrace();
        } finally {
            if (br != null)
                br.close();
        }
        System.out.println("map size: " + firstLayerNeighborCountMap.size());

        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(to_find), "UTF-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        String toFind = br2.readLine();
        try {
            while (toFind != null) {
                String result = firstLayerNeighborCountMap.get(toFind.trim());
                if (result == null) {
                    toFind = br2.readLine();
                    continue;
                }
                bw.write(String.format("%s|%s", toFind, result));
                bw.newLine();
                toFind = br2.readLine();
            }
            bw.flush();
        } catch (Exception e) {
            System.out.println("to find: " + toFind);
            e.printStackTrace();
        } finally {
            if (br2 != null)
                br2.close();
            if (bw != null)
                bw.close();
        }
    }
}
