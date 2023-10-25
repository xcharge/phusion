package com.xcharge.test;
import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.ZhiAnSiChuang;
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
//        String applicationId = "ZhiAnSiChuang";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"http://yun.bolink.club/zld/chargingPile/syncChargePilePay\"}";
//
//        String connectionConfig = "{\"secret\":\"EB6X1Q6IA3UT5TJY\",\"unionId\":\"200129\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710976\",\n" +
//                "  \"parkId\": \"131707\",\n" +
//                "  \"carNo\": \"京AB21515\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"startTime\": \"2023-10-18 13:50:01\",\n" +
//                "    \"endTime\": \"2023-10-18 14:54:35\"\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(ZhiAnSiChuang.class.getName())
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
