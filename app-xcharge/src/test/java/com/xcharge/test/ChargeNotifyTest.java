package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.DBStorage;
import cloud.phusion.storage.Record;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.XCharge;
import com.xcharge.application.util.DBTool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChargeNotifyTest {
    private static String applicationId = "xcharge";
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
//        engine = TestUtil.buildPhusionEngine()
//                .needDBStorage(true)
//                .needWebServer(true, 9900)
//                .done();
//
//        engine.start(null);
//
//        DBTool.prepareChargeTable(applicationId, engine);
    }

    @Test
    public void testChargingNotification() throws Exception {
//        String clientId = "luyao";
//        String secretKey = "123456";
//        String endpointId = "chargingNotification";
//
//        String appConfig = "{\"maxSignTimeGap\":10000000000, \"storeChargeInfo\":true}";
//
//        String connectionConfig = "{\"client\":\""+clientId+"\", \"secretKey\":\""+secretKey+"\", " +
//                "\"storeChargeInfoForStations\":{\"660\":true}}";
//
//        String msg = "{\"duration\":37,\"tradeNo\":\"690893096315527168\",\"carNo\":\"京ADG7191\",\"tenantId\":\"JNBJ\"," +
//                "\"sign\":\"fe36a27fabae7ac1151bcdb69c6eb354\",\"signType\":\"MD5\",\"startTime\":1668133482000," +
//                "\"endTime\":1668135712000,\"parkId\":\"660\",\"timestamp\":1678086750165,\"powerCharged\":22.3}";
//
//        String mockedResult = "{\"code\": 200, \"desc\": \"SUCCESS\"}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(XCharge.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(appConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .setIntegrationMockedResult(mockedResult)
//                .done();
//
//        String url = "http://localhost:9900/"+applicationId+"/notification/"+clientId;
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
//        assertTrue(objBody.containsKey("code"));
//
//        TestUtil.unregisterApplication(engine, applicationId);
//
//        Record[] records = _queryCharges(applicationId, "charges", "690893096315527168");
//        System.out.println();
//        System.out.println(records[0].toJSONString());
//        assertEquals(1, records.length);
    }

    private Record[] _queryCharges(String id, String table, String chargingId) throws Exception {
        DBStorage storage = engine.getDBStorageForApplication(id);
        ArrayList<Object> params = new ArrayList<>();
        params.add(chargingId);
        return storage.queryRecords(table, null, "chargingId=?", params);
    }

    @AfterClass
    public static void tearDown() throws Exception {
//        engine.stop(null);
    }

}
