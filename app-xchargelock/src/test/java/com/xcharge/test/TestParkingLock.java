package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.XChargeParkingLock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URLEncoder;

import static org.junit.Assert.assertTrue;

public class TestParkingLock {
    private static Engine engine;
    private static String applicationId = "XChargeParkingLock";

    @BeforeClass
    public static void setup() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .needWebServer(true,9090)
                .done();

        engine.start(null);
    }

    @AfterClass
    public static void teardown() throws Exception {
        engine.stop(null);
    }

    @Test
    public void statusNotification() throws Exception {
        String endpointId = "statusNotification";
        String applicationConfig = "{\"serviceUrl\": \"https://baidu.com/\"}";
        String connectionConfig = "{}";

        String msg = "{" +
                "       \"joinId\":\"123456\"," +
                "       \"lockId\":\"4330113901\"," +
                "       \"lockStatus\":\"unknown\"," +
                "       \"carParking\":\"false\"," +
                "       \"lockReleased\":false," +
                "       \"online\":false," +
                "       \"batteryPower\":95" +
                "}";

        TestUtil.registerApplication()
                .setEngine(engine)
                .setApplicationClass(XChargeParkingLock.class.getName())
                .setApplicationId(applicationId)
                .setApplicationConfig(applicationConfig)
                .setConnectionConfig(connectionConfig)
                .setEndpointToTest(endpointId)
                .done();

        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);

        System.out.println();
        System.out.println("Result: " + result);
        System.out.println();

        JSONObject objMsg = JSON.parseObject(result);
        assertTrue(objMsg.containsKey("code"));

        TestUtil.unregisterApplication(engine, applicationId);
    }

    public static void main(String[] args) throws Exception {

        String msg = "{\"spaceId\":\"11010558E9\",\"sdkId\":\"cf9d2953371373\",\"lockCode\":\"hk\",\"token\":\"abcdefgh\"}";
        msg = "info=" + URLEncoder.encode(msg, "UTF-8");
        System.out.println(msg);
    }
    @Test
    public void testPerformAction() throws Exception {
        String clientId = "luyao";
        String endpointId = "performAction";
        String appConfig = "{}";
        String connectionConfig = "{\"client\":\""+clientId+"\"}";

        String msg = "{\"spaceId\":\"11010558E9\",\"sdkId\":\"cf9d2953371373\",\"lockCode\":\"hk\",\"token\":\"abcdefgh\"}";
        msg = "info=" + URLEncoder.encode(msg, "UTF-8");

        String mockedResult = "{\"code\":\"ok\",\"desc\":\"\",\"info\":" +
                "{\"lockId\":\"11010558E9\",\"lockStatus\":\"normal\",\"carParking\":\"unknown\",\"lockReleased\":true,\"online\":true,\"batteryPower\":95}}";

        TestUtil.registerApplication()
                .setEngine(engine)
                .setApplicationClass(XChargeParkingLock.class.getName())
                .setApplicationId(applicationId)
                .setApplicationConfig(appConfig)
                .setConnectionConfig(connectionConfig)
                .setEndpointToTest(endpointId)
                .setIntegrationMockedResult(mockedResult)
                .done();

        String url = "http://localhost:9090/"+applicationId+"/action/"+clientId+"/12345678";

        HttpClient http = engine.createHttpClient();
        HttpResponse response = http.post(url)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .body(msg)
                .send();

        System.out.println();
        System.out.println("HTTP Response: " + response.getBody().getString());
        System.out.println();

        TestUtil.unregisterApplication(engine, applicationId);
    }
}
