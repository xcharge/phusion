package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.KeyTop;
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
//        String applicationId = "KeyTop";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"http://kp-open.keytop.cn/unite-api/api/wec/SyncChargePilePay\"}";
//
//        String connectionConfig = "{\"appId\":\"11538\", \"secret\":\"123456\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710976\",\n" +
//                "  \"parkId\": \"6641\",\n" +
//                "  \"carNo\": \"京BD39952\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"startTime\": \"2023-15-22 19:54:01\",\n" +
//                "    \"endTime\": \"2023-15-32 21:12:35\"\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(KeyTop.class.getName())
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
