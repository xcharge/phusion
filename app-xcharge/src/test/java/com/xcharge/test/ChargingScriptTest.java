package com.xcharge.test;

import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.integration.Processor;
import cloud.phusion.integration.Transaction;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChargingScriptTest {
    private static Engine engine;
    private static String integrationConfig;

    @BeforeClass
    public static void setUp() throws Exception {
//        engine = TestUtil.buildPhusionEngine()
//                .done();
//        engine.start(null);
//
//        integrationConfig = "{\n" +
//                "   \"discountType\": 1,\n" +
//                "   \"discountValue\": 120,\n" +
//                "   \"discountValues\": [\n" +
//                "       {\"minPower\": 20.0, \"value\": 360},\n" +
//                "       {\"minPower\": 10.0, \"value\": 240}\n" +
//                "   ],\n" +
//                "   \"minDurationInMinutes\": 1,\n" +
//                "   \"hoursToExpire\": 3,\n" +
//                "   \"hoursToRediscount\": 12,\n" +
//                "   \"washMode\": 1,\n" +
//                "   \"specialStations\": {" +
//                "       \"601\":{\"hoursToExpire\":1, \"discountValues\":[{\"minPower\":10.0, \"value\":500}]}\n" +
//                "   },\n" +
//                "   \"extraInfoSource\": {\n" +
//                "       \"config\": [\"hoursToRediscount\"],\n" +
//                "       \"properties\": [\"name\"],\n" +
//                "       \"msg\": [\"stationOwner\"]\n" +
//                "   }\n" +
//                "}";
    }

    // Install phusion module in the project home:
    //      npm install --save-prod <path-to-phusion>

    @Test
    public void testBefore() throws Exception {
//        Transaction trx = TestUtil.buildTransaction()
//                                .setEngine(engine)
//                                .setIntegrationConfig(integrationConfig)
//                                .setCurrentStep("before")
//                                .done();
//
//        String incomingMessage = "{\n" +
//                "    \"stationOwner\": \"ABC\",\n" +
//                "    \"stationId\": \"601\",\n" +
//                "    \"chargingId\": \"6101234345467\",\n" +
//                "    \"chargingAuxId\": \"12100001\",\n" +
//                "    \"deviceId\": \"string\",\n" +
//                "    \"deviceType\": \"fastCharger\",\n" +
//                "    \"carNo\": \"京A897001\",\n" +
//                "    \"mobile1\": \"13501170200\",\n" +
//                "    \"userId1\": \"1234567890\",\n" +
//                "    \"userType1\": \"wechat\",\n" +
//                "    \"powerCharged\": 20.5,\n" +
//                "    \"duration\": 10,\n" +
//                "    \"startTime\": \"2023-03-19 20:02:15\",\n" +
//                "    \"endTime\": \"2023-03-19 20:02:25\"\n" +
//                "}";
//        trx.setMessage(new DataObject(incomingMessage));
//
//        trx.setProperty("name", "xcharge");
//
////        KVStorage storage = trx.getContext().getEngine().getKVStorageForIntegration(trx.getIntegrationId());
////        storage.put("IssuedCoupon"+"京A897001", "123456", 3 * 60 * 60 * 1000);
//
//        TestUtil.runScriptWithinTransaction("ChargingCarWash.node.js", trx);
//
//        System.out.println();
//        System.out.println(trx.getMessage().getString());
//        System.out.println();
    }

    @Test
    public void testAfter() throws Exception {
//        Transaction trx = TestUtil.buildTransaction()
//                .setEngine(engine)
//                .setIntegrationConfig(integrationConfig)
//                .setPreviousStep("before")
//                .setCurrentStep("after")
//                .done();
//
//        String incomingMessage = "{\n" +
//                "    \"code\": \"ok\",\n" +
//                "    \"desc\": \"处理成功\",\n" +
//                "    \"data\": {\"discountId\":\"123456\"}\n" +
//                "}";
//        trx.setMessage(new DataObject(incomingMessage));
//
//        trx.setProperty("carNo", "京A897001");
//
//        TestUtil.runScriptWithinTransaction("ChargingParking.node.js", trx);
//
//        System.out.println();
//        System.out.println(trx.getMessage().getString());
//        System.out.println();
//
////        KVStorage storage = trx.getContext().getEngine().getKVStorageForIntegration(trx.getIntegrationId());
////        System.out.println(storage.get("IssuedCoupon"+"京A897001"));
    }

    @Test
    public void testException() throws Exception {
//        Transaction trx = TestUtil.buildTransaction()
//                .setEngine(engine)
//                .setIntegrationConfig(integrationConfig)
//                .setPreviousStep("after")
//                .setCurrentStep("exception")
//                .done();
//
//        trx.setProperty("exception", "boom");
//
//        TestUtil.runScriptWithinTransaction("ChargingParking.node.js", trx);
//
//        System.out.println();
//        System.out.println(trx.getMessage().getString());
//        System.out.println();
    }

    @AfterClass
    public static void tearDown() throws Exception {
//        engine.stop(null);
    }

}
