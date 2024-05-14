package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.JsLifeV3;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class IssueCouponTest {
    private static Engine engine;

//    @BeforeClass
//    public static void setUp() throws Exception {
//        String base = IssueCouponTest.class.getClassLoader().getResource("").getPath();
//        engine = TestUtil.buildPhusionEngine()
//                .done();
//        engine.start(null);
//    }

//    @Test
//    public void testParkingDiscount() throws Exception {
//        String applicationId = "JsLifeV3";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"https://jsopen.jslife.com.cn/\"}";
//
//        String connectionConfig = "{\"appId\":\"12398811111\",\"cid\":\"0000111111\", \"projectCode\":\"p220\",\"abilityCode\":\"kfpt_syfw\",\"secret\":\"FAeVf5qHAUY0GvykQB\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710976\",\n" +
//                "  \"parkId\": \"p220245108\",\n" +
//                "  \"carNo\": \"川AAN4131\",\n" +
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
//                .setApplicationClass(JsLifeV3.class.getName())
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
//        System.out.println(objMsg.toJSONString());
//
//        TestUtil.unregisterApplication(engine, applicationId);
//    }

//    @Test
//    public void testUploadCert() throws Exception {
//        String endpointId = "uploadCertificate";
//        String applicationId = "jslifev3";
//        String applicationConfig = "{\"serviceUrl\": \"https://test-jsopen.jslifee.com.cn/openApi\"}";
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(JsLifeV3.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//        Thread.sleep(10000000L);
//
//        TestUtil.unregisterApplication(engine, applicationId);
//    }

//    @AfterClass
//    public static void tearDown() throws Exception {
//        engine.stop(null);
//    }

}
