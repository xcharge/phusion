package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.DingDing;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestParkingLock {
//    private static Engine engine;
//    private static String applicationId = "DingDingLock";
//
//    @BeforeClass
//    public static void setup() throws Exception {
//        engine = TestUtil.buildPhusionEngine()
//                .needWebServer(true,9090)
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
//        String applicationConfig = "{\"serviceUrl\": \"http://baidu.com/\"}";
//        String connectionConfig = "{\"appId\":\"123\", \"secret\":\"4567\"}";
//
//        String msg = "{\n" +
//                "  \"lockId\": \"4330113901\",\n" +
//                "  \"action\": \"lock\"" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(DingDing.class.getName())
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
//        String connectionConfig = "{\"client\": \""+clientId+"\"}";
//
//        String msg = "appId=ED13&event=0&lockState=2&parkingState=1&lockNum=DNFE3C&outDeviceNum=4330113901&isOutLift=0&outLiftNum=232323&liftTime=20230523181818&timestamp=1678086750165&nonceStr=dfsfdsfdsfsf&sign=fdsfsfdsfsdfsdf";
//
//        String mockedResult = "{\"code\": \"ok\", \"desc\": \"SUCCESS\"}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(DingDing.class.getName())
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
//                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                .body(msg)
//                .send();
//
//        System.out.println();
//        System.out.println("HTTP Response: " + response.getBody().getString());
//        System.out.println();
//
//        JSONObject objBody = response.getBody().getJSONObject();
//        assertTrue(objBody.containsKey("errCode"));
//
//        TestUtil.unregisterApplication(engine, applicationId);
//
//    }
}
