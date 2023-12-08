package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.JsLife;
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
//        String applicationId = "JsLife";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"https://jtc-test.jslife.com.cn/jparking-api/charge/sync_order_deduct\"}";
//
//        String connectionConfig = "{\"appId\":\"4ba45c3f003611\", \"secret\":\"fecb2590003611eab44\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710976\",\n" +
//                "  \"parkId\": \"27941418923030910171193534937\",\n" +
//                "  \"carNo\": \"藏ZJS1122\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"startTime\": \"2023-07-12 10:54:01\",\n" +
//                "    \"endTime\": \"2023-07-12 11:12:35\"\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(JsLife.class.getName())
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
