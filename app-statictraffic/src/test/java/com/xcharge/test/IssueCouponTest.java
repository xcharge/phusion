package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.StaticTraffic;
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
    public void testParkingDiscount() throws Exception {
//        String applicationId = "StaticTraffic";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"http://parkbay.bj-jtjt.cn/ke_access/derate\"}";
//
//        String connectionConfig = "{\"key\": \"123\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678628407468630016\",\n" +
//                "  \"parkId\": \"110228300011\",\n" +
//                "  \"carNo\": \"京AAM5187\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"startTime\": \"2022-11-22 19:54:01\",\n" +
//                "    \"endTime\": \"2022-11-22 21:12:35\"\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(StaticTraffic.class.getName())
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
