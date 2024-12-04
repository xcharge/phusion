package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.ZhiWeiYun;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IssueCouponTest {
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .done();
        engine.start(null);
    }

    @Test
    public void testDiscount() throws Exception {
        String applicationId = "zhiweiyun";
        String endpointId = "requestParkingDiscount";

        String applicationConfig = "{\"serviceUrl\": \"https://123-123.123-123.com/123/123\"}";

        String connectionConfig = "{\"appId\":\"123\",\"appAuthToken\":\"123\"}";

        String msg = "{\n" +
                "  \"requestId\": \"abc\",\n" +
                "  \"parkId\": \"abc \",\n" +
                "  \"carNo\": \"abc\",\n" +
                "  \"extraInfo\": {\n" +
                "    \"startTime\": \"2024-11-13 13:50:01\",\n" +
                "    \"endTime\": \"2024-11-13 14:54:35\"\n" +
                "  },\n" +
                "  \"type\": 1,\n" +
                "  \"value\": 5\n" +
                "}";

        TestUtil.registerApplication()
                .setEngine(engine)
                .setApplicationClass(ZhiWeiYun.class.getName())
                .setApplicationId(applicationId)
                .setApplicationConfig(applicationConfig)
                .setConnectionConfig(connectionConfig)
                .setEndpointToTest(endpointId)
                .done();

        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);

        System.out.println();
        System.out.println("Result: " + result);
        System.out.println();

        JSONObject objMsg = JSON.parseObject(result);
        assertTrue(objMsg.containsKey("code"));

        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
