package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.storage.DBStorage;
import cloud.phusion.storage.Record;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.XCharge;
import com.xcharge.application.util.DBTool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ChargeQueryTest {
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
//
//        _prepareMockData(applicationId, new String[]{
//                "{\"chargingId\":\"1001\", \"stationId\":\"660\", \"carNo\":\"京ADG7191\", \"duration\":10, \"startTime\":\"2022-11-11 10:00:00\", \"endTime\":\"2022-11-11 11:00:00\"}",
//                "{\"chargingId\":\"1002\", \"stationId\":\"660\", \"carNo\":\"京ADG7191\", \"duration\":10, \"startTime\":\"2022-11-11 11:00:00\", \"endTime\":\"2022-11-11 11:00:00\"}"
//        });
    }

    private static void _prepareMockData(String id, String[] records) throws Exception {
        DBStorage storage = engine.getDBStorageForApplication(id);

        for (int i = 0; i < records.length; i++) {
            storage.insertRecord(DBTool.chargeTable, new Record(records[i]));
        }
    }

    @Test
    public void testQueryChargingInfo() throws Exception {
//        String endpointId = "queryChargingInfo";
//
//        String appConfig = "{\"maxSignTimeGap\":10000000000, \"storeChargeInfo\":true}";
//
//        String msg = "{\"stationId\":\"660\", \"carNo\":\"京ADG7191\", \"timeFrom\":\"2022-11-11 10:00:00\", \"timeTo\":\"2022-11-11 11:00:00\"}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(XCharge.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(appConfig)
//                .setConnectionConfig(null)
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
//        assertTrue(objMsg.containsKey("result"));
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
//        engine.stop(null);
    }

}
