package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.BeiTou;
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
        String applicationId = "BeiTou";
        String endpointId = "requestParkingDiscount";

        String applicationConfig = "{\"serviceUrl\": \"https://parking.big-btjtjt.com/start/\"}";

        String connectionConfig = "{\"key\":\"DTEST\",\"secret\":\"A95A84BB4705423C8F3C59A6DCF12345\"}";

        String msg = "{\n" +
                "  \"requestId\": \"969696268654223360\",\n" +
                "  \"parkId\": \"aaaabbbccc\",\n" +
                "  \"carNo\": \"京ABC123\",\n" +
                "  \"extraInfo\": {\n" +
                "    \"startTime\": \"2016-11-04 16:56:50\",\n" +
                "    \"endTime\": \"2023-10-18 14:54:35\"\n" +
                "  },\n" +
                "  \"type\": 1,\n" +
                "  \"value\": 60\n" +
                "}";

        TestUtil.registerApplication()
                .setEngine(engine)
                .setApplicationClass(BeiTou.class.getName())
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