package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.Wit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class IssueCouponTest {
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .needKVStorage(true)
                .needScheduler(true)
                .done();
        engine.start(null);
    }

    @Test
    public void testParkingDiscount() throws Exception {
//        String applicationId = "wit";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"http://sysapitest.wit-parking.com/api/chargePlatform\"}";
//
//        String connectionConfig = "{\"OperatorID\":\"sl168601329\", \"OperatorSecret\":\"51F6F6CB7FD47B6ED\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710976\",\n" +
//                "  \"parkId\": \"370202001262\",\n" +
//                "  \"carNo\": \"京ALG001\",\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120\n" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Wit.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//
//        // 第一次发送错误的车牌
//        System.out.println("---------------------- 1st Time: Should fail");
//        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        JSONObject objMsg = JSON.parseObject(result);
//        Assert.assertNotEquals(objMsg.getString("code"), "ok");
//
//        // 改变正确车牌会成功
//        System.out.println("---------------------- 2nd Time: Should success");
//        msg = msg.replace("京ALG001", "鲁ZLG001");
//        TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        // 再发一次会失败
//        System.out.println("---------------------- 3rd Time: Should fail");
//        TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        // 改变车牌再发一次成功
//        System.out.println("---------------------- 4th Time: Should success");
//        msg = msg.replace("鲁ZLG001", "鲁ZLG002");
//        TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
