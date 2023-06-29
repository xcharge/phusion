package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author zh
 * @date 2023/6/2 11:06
 */
public class TokenTest {
    private static Engine engine;
    private static final String applicationId = "greenTown";

    @BeforeClass
    public static void setUp() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .needWebServer(true, 9901)
                .needKVStorage(true)
                .needScheduler(true)
                .done();
        engine.start(null);
    }


    @Test
    public void testToken() throws Exception {
//        String applicationConfig = "{" +
//                "\"serviceUrl\": \"https://lydev-ipms.gttis.cc/couponNotice\"" +
//                "}";
//
//        String connectionConfig = "{\"OperatorID\":\"194400996\",\"OperatorSecret\":\"dVa5RXysztIHWJH0\",\"SecretKey\":\"ICxT5BNtVetuMJZf\",\"SecretIv\":\"pXzNnSZOUAAyzWgC\",\"SigSecret\":\"SFxv4yUssQ42GssK\"}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(GreenTown.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .done();
//
//        Thread.sleep(1000); // Wait for the token to be refreshed
//
//        KVStorage storage = engine.getKVStorageForApplication(applicationId);
//        String token = (String) storage.get("Token-" + TestUtil.getConnectionId(applicationId), null);
//
//        assertNotNull(token);
//
//        Thread.sleep(11 * 60 * 1000L);
//        String token2 = (String) storage.get("Token-" + TestUtil.getConnectionId(applicationId), null);
//
//        assertNotEquals(token2, token);
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }
}
