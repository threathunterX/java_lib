package com.threathunter.geo;

import org.junit.Test;

import java.io.*;

/**
 * created by www.threathunter.cn
 */
public class TestPhoneNumber {
    @Test
    public void testPhoneNumber() {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            String path = "C:\\Users\\daisy\\Desktop\\mobile_location_1.txt";
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            String outPath = "C:\\Users\\daisy\\Desktop\\diff.txt";
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath), "UTF-8"));

            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                String[] inputlist = line.split("\t");
                String number = inputlist[0] + "0001";
                String output = PhoneNumberLocaler.getCarrier(number, "86");
                String[] outlist = output.split("\t");
//                if (outlist.length < 2) {
//                    bw.write(String.format("%s\t%s\t%s\tdiffer\t%s", inputlist[0], inputlist[1], inputlist[2], output));
//                    bw.newLine();
//                    line = br.readLine();
//                    continue;
//                }
                String inputPlace = inputlist[1].substring(1, inputlist[1].length() - 2);
//                String inputCarrie = inputlist[2].substring(1, inputlist[2].length() - 1);
                boolean samePlace = outlist[0].contains(inputPlace);
//                boolean sameCarrie = outlist[1].equals(inputCarrie);
                if (!(outlist[0].contains(inputPlace) || inputPlace.contains(outlist[0]))) {
                    bw.write(String.format("%s\t%s\t%s\tdiffer\t%s", inputlist[0], inputlist[1], inputlist[2], outlist[0]));
                    bw.newLine();
                }
                line = br.readLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
