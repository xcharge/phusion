package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xcharge.Hmzhtc;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IssueCouponTest {
//    private static Engine engine;
//
//    @BeforeClass
//    public static void setUp() throws Exception {
//        engine = TestUtil.buildPhusionEngine()
//                .done();
//        engine.start(null);
//    }
//
//    @Test
//    public void testParkingDiscount() throws Exception {
//        String applicationId = "Hmzhtc";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"https://hpkiot.hmzhtc.com/api/exemption/charging/public/v1/notify-charge-order\"}";
//
//            String connectionConfig = "{\"appId\":\"1\", \"secret\":\"B\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710980\",\n" +
//                "  \"parkId\": \"100749\",\n" +
//                "  \"parkName\":\"130车场\",\n" +
//                "  \"carNo\": \"粤BAB3333\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"startTime\": \"2025-07-06 14:04:01\",\n" +
//                "    \"endTime\": \"2025-07-06 15:04:01\"\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Hmzhtc.class.getName())
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
//    }
//
//    @AfterClass
//    public static void tearDown() throws Exception {
//        engine.stop(null);
//    }

}
