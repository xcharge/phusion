package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.HaoTingChe;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class IssueCouponTest {
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .needKVStorage(true)
                .done();
        engine.start(null);
    }

    @Test
    public void testParkingDiscount() throws Exception {
        String applicationId = "haotingche";
        String endpointId = "requestParkingDiscount";

        String applicationConfig = "{\"serviceUrl\": \"http://abc.4-abc.com\"}";

        String connectionConfig = "{\"OperatorID\":\"aaaa\",\"OperatorSecret\":\"bbbb\",\"AES_KEY\":\"cccc\",\"AES_IV\":\"eeee\",\"SIGN_KEY\":\"dddd\"}";

        String msg = "{\n" +
                "  \"requestId\": \"123456789012345679\",\n" +
                "  \"parkId\": \"1\",\n" +
                "  \"carNo\": \"粤A10012\",\n" +
                "  \"type\": 1,\n" +
                "  \"value\": 120,\n" +
                "  \"extraInfo\": {\n" +
                "    \"startTime\": \"2025-10-11 10:34:36\",\n" +
                "    \"endTime\": \"2025-10-11 10:35:44\"\n" +
                "  }\n" +
                "}";


        TestUtil.registerApplication()
                .setEngine(engine)
                .setApplicationClass(HaoTingChe.class.getName())
                .setApplicationId(applicationId)
                .setApplicationConfig(applicationConfig)
                .setConnectionConfig(connectionConfig)
                .setEndpointToTest(endpointId)
                .done();


        System.out.println("---------------------- Test: Request parking discount");
        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);

        System.out.println();
        System.out.println("Result: " + result);
        System.out.println();

        JSONObject objMsg = JSON.parseObject(result);
        Assert.assertEquals(objMsg.getString("code"), "ok");

        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
