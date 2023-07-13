package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.GreenTown;
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
//        String applicationId = "greenTown";
//        String endpointId = "requestParkingDiscount";
//
//        String applicationConfig = "{\"serviceUrl\": \"https://lydev-ipms.gttis.cc/couponNotice\"}";
//
//        String connectionConfig = "{\"OperatorID\":\"194400996\",\"OperatorSecret\":\"dVa5RXysztIHWJH0\",\"SecretKey\":\"ICxT5BNtVetuMJZf\",\"SecretIv\":\"pXzNnSZOUAAyzWgC\",\"SigSecret\":\"SFxv4yUssQ42GssK\"}";
//
//        String msg = "{\n" +
//                "  \"requestId\": \"678539775491710976\",\n" +
//                "  \"parkId\": \"c35d1364-b8e6-11ed-9048-000c293f717e\",\n" +
//                "  \"carNo\": \"鲁ZLG001\",\n" +
//                "  \"type\": 1,\n" +
//                "  \"value\": 120,\n" +
//                "  \"extraInfo\": {\n" +
//                "    \"startTime\": \"2023-06-26 13:54:01\",\n" +
//                "    \"endTime\": \"2023-06-27 12:54:01\"\n" +
//                "  }\n" +
//                "}";
//
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(GreenTown.class.getName())
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
//        msg = msg.replace("鲁ZLG001", "浙A112233");
//        result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        objMsg = JSON.parseObject(result);
//        Assert.assertEquals(objMsg.getString("code"), "ok");
//
//        // 再发一次会成功
//        System.out.println("---------------------- 3rd Time: Should fail");
//        result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        objMsg = JSON.parseObject(result);
//        Assert.assertEquals(objMsg.getString("code"), "ok");
//
//        // 改变车牌再发一次成功
//        System.out.println("---------------------- 4th Time: Should success");
//        msg = msg.replace("浙A112233", "浙A112244");
//        result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        objMsg = JSON.parseObject(result);
//        Assert.assertEquals(objMsg.getString("code"), "ok");
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
