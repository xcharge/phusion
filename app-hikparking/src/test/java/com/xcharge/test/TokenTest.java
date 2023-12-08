package com.xcharge.test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import cloud.phusion.storage.KVStorage;
import com.xcharge.application.Hikparking;
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
    private static final String applicationId = "hikparking";

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
//                "\"serviceUrl\": \"http://api.hikparking.com:9016/artemis\"" +
//                "}";
//
//        String connectionConfig = "{" +
//                "\"clientId\": \"261\"," +
//                "\"clientSecret\": \"frG1dgrpZP\"" +
//                "}";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Hikparking.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .done();
//
//        Thread.sleep(1000); // Wait for the token to be refreshed
//
//        KVStorage storage = engine.getKVStorageForApplication(applicationId);
//        String token = (String) storage.get("Token-" + TestUtil.getConnectionId(applicationId), null);
//        System.out.println("token:" + token);
//
//        assertNotNull(token);
//
//        Thread.sleep(11 * 60 * 1000L);
//        String token2 = (String) storage.get("Token-" + TestUtil.getConnectionId(applicationId), null);
//        System.out.println("token2:" + token);
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
