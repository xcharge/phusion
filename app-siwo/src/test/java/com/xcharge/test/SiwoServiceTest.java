package com.xcharge.test;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.EngineFactory;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.Siwo;
import com.xcharge.application.impl.SiwoService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class SiwoServiceTest {
    private static String applicationId;
    private static String applicationConfig;

    private static Engine engine;
    private static SiwoService service;
    private static Context ctx;

    @BeforeClass
    public static void setUp() throws Exception {
//        String base = SiwoServiceTest.class.getClassLoader().getResource("").getPath();
//
//        engine = TestUtil.buildPhusionEngine()
//                .needKVStorage(true)
//                .needScheduler(true)
//                .needFileStorage(true, base+"FileStorage/private", null, null)
//                .done();
//        engine.start(null);
//
//        applicationId = "siwo";
//
//        applicationConfig = "{" +
//                "  \"serviceUrl\": \"http://www.siwo-pps.com/api.ashx\", " +
//                "  \"intervalTokenExpire\": 5 " +
//                "}";
//
//        JSONObject config = JSON.parseObject(applicationConfig);
//        service = new SiwoService(applicationId, config.getString("serviceUrl"),
//                config.getIntValue("intervalTokenExpire"));
//
//        ctx = EngineFactory.createContext(engine);
    }

    @Test
    public void testRetrieveToken() throws Exception {
//        String result = service.retrieveToken("ftx7", "/certificates/fenbu.pem", ctx);
//        System.out.println();
//        System.out.println(result);
    }

    @Test
    public void testIssueCoupon() throws Exception {
//        JSONObject result = service.issueCoupon(
//                1, 120, "2022-12-02", "京Q58888",
//                "ftx7", "/certificates/fenbu.pem", ctx);
//        System.out.println();
//        System.out.println(result.toJSONString());
    }

    @Test
    public void testQueryCoupon() throws Exception {
//        JSONObject result = service.queryValidCoupons("京Q58888","dxt3", "/certificates/fenbu.pem", ctx);
//        System.out.println();
//        System.out.println(result.toJSONString());
    }

    @Test
    public void testCancelCoupon() throws Exception {
//        String couponId = "22534";
//        service.cancelCoupon(couponId,"dxt3", "/certificates/fenbu.pem", ctx);
//
//        JSONObject result = service.queryValidCoupons("京Q58888","dxt3", "/certificates/fenbu.pem", ctx);
//        assertTrue( result.toJSONString().indexOf(couponId) < 0 );
    }

    @Test
    public void testAutoRefreshToken() throws Exception {
//        engine.registerApplication(applicationId, "com.xcharge.application.Siwo",
//                new DataObject(applicationConfig), ctx);
//
//        String park = "dxt3";
//        Siwo app = (Siwo) engine.getApplication(applicationId);
//        app.addPark(park, "/certificates/fenbu.pem");
//        app.start(ctx);
//
//        Thread.sleep(15000);
//
//        KVStorage storage = engine.getKVStorageForApplication(applicationId);
//        System.out.println();
//        System.out.println(storage.get("Token-"+park));
    }

    @AfterClass
    public static void tearDown() throws Exception {
//        engine.stop(null);
    }

}
