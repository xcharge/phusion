package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.TJDParking;
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
    public void testIssueCoupon() throws Exception {
//        String applicationId = "TJDParking";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"http://api.tingjiandan.com/openapi/gateway\"}";
//
//        String connectionConfig = "{\"partner\":\"7ac00f07a06444f7819f00dd3f3b9411\", \"secret\":\"123456\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678628407468630016\",\n" +
//                "  \"parkId\": \"110228300011\",\n" +
//                "  \"carNo\": \"京AAM5187\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"channel\": 2206\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(TJDParking.class.getName())
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
