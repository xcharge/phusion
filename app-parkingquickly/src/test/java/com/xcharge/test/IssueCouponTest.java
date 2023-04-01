package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.ParkingQuickly;
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
//        String applicationId = "ParkingQuickly";
//        String endpointId = "requestParkingDiscount";
//
//        String connectionConfig = "{" +
//                "  \"secret\": \"123456\"," +
//                "  \"channel\": \"xcharge\"," +
//                "  \"serviceUrl\": \"http://cloud.parkingquickly.com:10080/openapi\"" +
//                "}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678628407468630016\",\n" +
//                "  \"parkId\": \"1006\",\n" +
//                "  \"carNo\": \"京AAM5187\",\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(ParkingQuickly.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(null)
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
