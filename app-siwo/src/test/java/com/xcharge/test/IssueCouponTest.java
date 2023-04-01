package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.Siwo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IssueCouponTest {
    private static String applicationId;
    private static String applicationConfig;
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
//        String base = IssueCouponTest.class.getClassLoader().getResource("").getPath();
//
//        engine = TestUtil.buildPhusionEngine()
//                .needKVStorage(true)
//                .needScheduler(true)
//                .needFileStorage(true, base+"FileStorage/private", null, null)
//                .needWebServer(true, 9900)
//                .done();
//        engine.start(null);
//
//        applicationId = "siwo";
//
//        applicationConfig = "{" +
//                "  \"serviceUrl\": \"http://www.siwo-pps.com/api.ashx\", " +
//                "  \"intervalTokenExpire\": 900 " +
//                "}";
    }

    @Test
    public void testUploadCert() throws Exception {
//        String endpointId = "uploadCertificate";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Siwo.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//        Thread.sleep(10000000L);
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @Test
    public void testParkingDiscount() throws Exception {
//        String endpointId = "requestParkingDiscount";
//
//        String connectionConfig = "{\"keyPath\":\"/certificates/fenbu.pem\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678628407468630016\",\n" +
//                "  \"parkId\": \"hd1\",\n" +
//                "  \"carNo\": \"京BD39952\",\n" +
//                "  \"expireTime\": \"2023-03-08 16:41:00\",\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"hoursToRediscount\": 3\n" +
//                "  },\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Siwo.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//        System.out.println("---------------------- 1st Time: Should succeed");
//        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        JSONObject objMsg = JSON.parseObject(result);
//        assertTrue(objMsg.containsKey("code"));
//
//        // 前面第一次发成功了
//        // 再发一次会失败
//        System.out.println("---------------------- 2nd Time: Should fail");
//        TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        // 改变车牌再发一次能成功
//        System.out.println("---------------------- 3rd Time: Should clear and reissue");
//        msg = msg.replace("京BD39952", "京BD39953");
//        TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        // 再发一次又会失败
//        System.out.println("---------------------- 4th Time: Should fail");
//        TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
//        engine.stop(null);
    }

}
