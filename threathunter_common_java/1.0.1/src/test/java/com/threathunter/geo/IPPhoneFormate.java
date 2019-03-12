package com.threathunter.geo;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class IPPhoneFormate {

    @Test
    public void testSplit() {
        String string = "湖南省湘西土家族苗族自治州";
        if (string.endsWith("自治州")) {
            System.out.println(string.substring(0, string.length()-1));
            System.out.println("end with 自治州");
        }
//        else if (string.endsWith(""))
    }

}
