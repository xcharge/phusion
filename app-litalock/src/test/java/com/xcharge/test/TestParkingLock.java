package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.xcharge.application.Lita;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestParkingLock {
//    private static Engine engine;
//    private static final String applicationId = "LitaLock";
//
//    @BeforeClass
//    public static void setup() throws Exception {
//        engine = TestUtil.buildPhusionEngine()
//                .needWebServer(true, 9090)
//                .done();
//
//        engine.start(null);
//    }
//
//    @AfterClass
//    public static void teardown() throws Exception {
//        engine.stop(null);
//    }
//
//    @Test
//    public void testAction() throws Exception {
//        String endpointId = "performAction";
//        String applicationConfig = "{\"serviceUrl\": \"https://newparking.litaparking.com/api/openapi/\"}";
//        String connectionConfig = "{\"appId\":\"FJXih5ZwAW\", \"appKey\":\"ixtjFZdjfM8BXaAQGdsQ\"}";
//
//        String msg = "{\n" +
//                "  \"lockId\": \"6688202000112701\",\n" +
//                "  \"action\": \"queryStatus\"" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Lita.class.getName())
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
//    @Test
//    public void testNotify() throws Exception {
//        String clientId = "luyao";
//        String endpointId = "statusNotification";
//        String appConfig = "{}";
//        String connectionConfig = "{\"client\": \"" + clientId + "\"}";
//
//        String msg = "{\"notifytype\":2, \"macno\":\"11111\",\"type\":\"1\",\"battery\":90,\"lockStatus\":2,\"hasCar\":1,\"errortext\":\"错误信息\"}";
//
//        String mockedResult = "{\"code\":0, \"message\": \"\"}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Lita.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(appConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .setIntegrationMockedResult(mockedResult)
//                .done();
//
//        String url = "http://localhost:9090/" + applicationId + "/notification/" + clientId + "/111111";
//
//        HttpClient http = engine.createHttpClient();
//        HttpResponse response = http.post(url)
//                .header("Content-Type", "application/json; charset=UTF-8")
//                .body(msg)
//                .send();
//
//        System.out.println();
//        System.out.println("HTTP Response: " + response.getBody().getString());
//        System.out.println();
//
//        JSONObject objBody = response.getBody().getJSONObject();
//        System.out.println(objBody.toJSONString());
//        assertTrue(objBody.containsKey("code"));
//
//        TestUtil.unregisterApplication(engine, applicationId);
//
//    }
}
