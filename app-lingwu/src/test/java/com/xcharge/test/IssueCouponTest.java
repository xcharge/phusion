package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.Lingwu;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class IssueCouponTest {
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .done();
        engine.start(null);
    }

    @Test
    public void testCarWashDiscount() throws Exception {
//        String applicationId = "Lingwu";
//        String endpointId = "requestCarWashDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"http://apiout.i7188.com/api\"}";
//
//        String connectionConfig = "{\"appKey\": \"123\", \"loginSecret\": \"456\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678628407468630017\",\n" +
//                "  \"expireTime\": \"2023-03-08 12:54:00\",\n" +
//                "  \"mobile\": \"13501102800\",\n" +
//                "  \"washMode\": 1,\n" +
//                "  \"type\": 0,\n" +
//                "  \"value\": 500\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Lingwu.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        JSONObject objMsg = JSON.parseObject(result);
//        assertTrue(objMsg.containsKey("code"));
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
