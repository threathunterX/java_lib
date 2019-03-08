package com.threathunter.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test with the Mockito, use PowerMockito here for more apis support such like {@code whenNew}
 * Remember to include the class that you want to take effect on in {@code PrepareForTest}.
 * In this class, if we ignore PrepareForTest, we will not get mock when {@code new URL()} happens
 * in URLConfigurationSource
 *
 * When test with input stream, because we need to verify update, then we need to renew a inputstream by
 * avoiding use thenReturn. {@code thenReturn} will return the same instance in spite of define new inputstream.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(com.netflix.config.sources.URLConfigurationSource.class)
public class CommonDynamicConfigTest {

    private static final String MY_URL = "http://localhost:8000/testUrl.conf";
    private static final String MY_URL_HIGHER = "http://localhost:8000/testUrlHigher.conf";

    private static final String MY_CONFIG_FILE = "test.conf";
    private static final String MY_CONFIG_FILE_HIGHER = "testhigher.conf";

    private URL my_file_url;
    private URL my_file_higher_url;

    private String myFileUrl = ConfigUtils.getFileUrl(MY_CONFIG_FILE)[0];
    private String myFileHigherUrl = ConfigUtils.getFileUrl(MY_CONFIG_FILE_HIGHER)[0];

    @Before
    public void setUp() throws MalformedURLException {
        my_file_url = new URL(myFileUrl);
        my_file_higher_url = new URL(myFileHigherUrl);
    }

    @Test
    public void testDefaultDynamicFileConfig() {
        // default config is config.properties
        Assert.assertEquals("myvalue", CommonDynamicConfig.getInstance().getString("mykey"));
    }

    @Test
    public void testDynamicFileConfig() {
        CommonDynamicConfig.getInstance().addConfigFile(MY_CONFIG_FILE);
        Assert.assertEquals("mytestValue", CommonDynamicConfig.getInstance().getString("mytestkey"));
        Assert.assertNull(CommonDynamicConfig.getInstance().getString("nonexistkey"));
    }

    @Test
    public void testMultipleFilesConfig() {
        CommonDynamicConfig.getInstance().addConfigFiles(MY_CONFIG_FILE, MY_CONFIG_FILE_HIGHER);
        Assert.assertEquals("onlyvalue", CommonDynamicConfig.getInstance().getString("myonlykey"));
        Assert.assertEquals("higheronlyvalue", CommonDynamicConfig.getInstance().getString("higheronlykey"));
        Assert.assertEquals("mytestValueHigher", CommonDynamicConfig.getInstance().getString("mytestkey"));
    }

    @Test
    public void testDynamicUrlConfig() throws Exception {
        enableMockUrl();

        CommonDynamicConfig.getInstance().addConfigUrl(MY_URL);
        Assert.assertEquals("myurlvalue", CommonDynamicConfig.getInstance().getString("myurlkey"));
    }

    @Test
    public void testMultipleUrlsConfig() throws Exception {
        enableMockUrl();
        enableMockUrlHigher();

        CommonDynamicConfig.getInstance().addConfigUrls(MY_URL, MY_URL_HIGHER);
        Assert.assertEquals("myurlhighervalue", CommonDynamicConfig.getInstance().getString("myurlkey"));
        Assert.assertEquals("myonlyurlvalue", CommonDynamicConfig.getInstance().getString("myonlykey"));
    }

    @Test
    public void testMultipleConfigSources() throws Exception {
        enableMockUrl();
        enableMockUrlHigher();
        CommonDynamicConfig.getInstance().addConfigFile(MY_CONFIG_FILE);
        CommonDynamicConfig.getInstance().addConfigFile(MY_CONFIG_FILE_HIGHER);
        CommonDynamicConfig.getInstance().addConfigUrl(MY_URL);

        Assert.assertEquals("myonlyurlvalue", CommonDynamicConfig.getInstance().getString("myonlykey"));
        Assert.assertEquals(3, CommonDynamicConfig.getInstance().getStringArray("keylist").length);
    }

    private void enableMockUrl() throws Exception {
        URL mockUrl = PowerMockito.mock(URL.class);

        PowerMockito.whenNew(URL.class).withArguments(MY_URL).then((Answer<URL>) invocationOnMock -> {
            System.out.println("mock success");
            return mockUrl;
        });
        PowerMockito.whenNew(URL.class).withArguments(myFileUrl).thenReturn(my_file_url);

        // avoid to use thenReturn, or when poller doing next update, there will be no content.
//        PowerMockito.when(mockUrl.openStream()).thenReturn(new ByteArrayInputStream("myurlkey=myurlvalue\r\nmyonlykey=myonlyvalue\r\n".getBytes()));
        PowerMockito.when(mockUrl.openStream()).then((Answer<InputStream>) is -> {
            InputStream inputStream = new ByteArrayInputStream("myurlkey=myurlvalue\r\nmyonlykey=myonlyurlvalue\r\n".getBytes());
            return inputStream;
        });
    }

    private void enableMockUrlHigher() throws Exception {
        URL mockUrl_higher = PowerMockito.mock(URL.class);

        PowerMockito.whenNew(URL.class).withArguments(MY_URL_HIGHER).thenReturn(mockUrl_higher);
        PowerMockito.whenNew(URL.class).withArguments(myFileHigherUrl).thenReturn(my_file_higher_url);

//        PowerMockito.when(mockUrl_higher.openStream()).thenReturn(new ByteArrayInputStream("myurlkey=myurlhighervalue\r\n".getBytes()))
        PowerMockito.when(mockUrl_higher.openStream()).then((Answer<InputStream>) is -> new ByteArrayInputStream("myurlkey=myurlhighervalue\r\n".getBytes()));
    }

    @Test
    public void testMockReDeclare() throws Exception {
        String localPath = "file://myfile";
        URL localUrl = new URL(localPath);
        URL mockUrl = PowerMockito.mock(URL.class);

        PowerMockito.whenNew(URL.class).withArguments(MY_URL).then((Answer<URL>) invocationOnMock -> {
            System.out.println("mock success");
            return mockUrl;
        });

        Assert.assertNull(new URL(localPath));

        PowerMockito.whenNew(URL.class).withArguments(localPath).then((Answer<URL>) invocationOnMock -> {
            System.out.println("mock success");
            return localUrl;
        });

        Assert.assertEquals(mockUrl, new URL(MY_URL));
        Assert.assertEquals(localUrl, new URL(localPath));
    }

    @Test
    public void testDynamicAddSingleProperty() {
        CommonDynamicConfig.getInstance().addConfigFile(MY_CONFIG_FILE);

        String observeKey = "myonlykey";
        CommonDynamicConfig.getInstance().addProperty(observeKey, "newvalue");
        Assert.assertEquals("onlyvalue", CommonDynamicConfig.getInstance().getString(observeKey));

        CommonDynamicConfig.getInstance().addOverrideProperty(observeKey, "overridevalue");
        Assert.assertEquals("overridevalue", CommonDynamicConfig.getInstance().getString(observeKey));
    }

    @Test
    public void testPropertyChangeCallback() throws InterruptedException {
        String observeKey = "myonlykey";
        List<String> callBackList = new ArrayList<>();
        CommonDynamicConfig.getInstance().addConfigFile(MY_CONFIG_FILE);
        CommonDynamicConfig.getInstance().addPropertyCallback(observeKey, () -> callBackList.add("callback"));
        CommonDynamicConfig.getInstance().addProperty(observeKey, "newvalue");
        Assert.assertEquals(0, callBackList.size());
        CommonDynamicConfig.getInstance().addOverrideProperty(observeKey, "callbackvalue");
        Assert.assertEquals(1, callBackList.size());
        Assert.assertEquals("callback", callBackList.get(0));
    }

    @Test
    public void testAddOverridePropertyByFile() {
        CommonDynamicConfig.getInstance().addOverridePropertyByFile("testhigher.conf");
        CommonDynamicConfig.getInstance().addConfigFile("test.conf");

        Assert.assertEquals("mytestValueHigher", CommonDynamicConfig.getInstance().getString("mytestkey"));
        Assert.assertEquals("higher", CommonDynamicConfig.getInstance().getString("overridekey"));

        CommonDynamicConfig.getInstance().addOverridePropertyByFile("test.conf");
        Assert.assertEquals("lower", CommonDynamicConfig.getInstance().getString("overridekey"));

        CommonDynamicConfig.getInstance().addOverridePropertyByFile("testhigher.conf");
        Assert.assertEquals("higher", CommonDynamicConfig.getInstance().getString("overridekey"));
    }

    @Test
    public void testAddOverridePropertyByUrl() {
        CommonDynamicConfig.getInstance().addOverridePropertyByUrl(myFileHigherUrl);
        CommonDynamicConfig.getInstance().addConfigUrl(myFileUrl);

        Assert.assertEquals("mytestValueHigher", CommonDynamicConfig.getInstance().getString("mytestkey"));
        Assert.assertEquals("higher", CommonDynamicConfig.getInstance().getString("overridekey"));

        CommonDynamicConfig.getInstance().addOverridePropertyByUrl(myFileUrl);
        Assert.assertEquals("lower", CommonDynamicConfig.getInstance().getString("overridekey"));

        CommonDynamicConfig.getInstance().addOverridePropertyByUrl(myFileHigherUrl);
        Assert.assertEquals("higher", CommonDynamicConfig.getInstance().getString("overridekey"));
    }

    @Test
    public void testList() {
        CommonDynamicConfig.getInstance().addConfigFile("test.conf");
        System.out.println(Arrays.asList(CommonDynamicConfig.getInstance().getStringArray("keylist")));
        System.out.println(Arrays.asList(CommonDynamicConfig.getInstance().getStringArray("redis_cluster")));
    }
}
