package com.xcharge.test;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.EngineFactory;
import cloud.phusion.application.Application;
import cloud.phusion.dev.test.TestUtil;
import cloud.phusion.storage.FileStorage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.Ezviz;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import static org.junit.Assert.*;

public class ApplicationTest {
    private static String applicationId;
    private static String applicationConfig;
    private static String connectionConfig;
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        String base = ApplicationTest.class.getClassLoader().getResource("").getPath();

        engine = TestUtil.buildPhusionEngine()
                .needKVStorage(true)
                .needFileStorage(true)
                .setPrivateFilePath(base + "FileStorage/private")
                .setPublicFilePath(base + "FileStorage/public")
                .done();
        engine.start(null);

        applicationId = "ezviz";

        applicationConfig = "{\"serviceUrl\": \"https://open.ys7.com/api/lapp\"}";

        connectionConfig = "{" +
                "\"appKey\": \"b925a43d90\", " +
                "\"appSecret\": \"34c43297d8e6\"" +
                "}";
    }

    @Test
    public void testRetrieveToken() throws Exception {
        Context ctx = EngineFactory.createContext(engine);

        engine.registerApplication(applicationId, "com.xcharge.application.Ezviz",
                new DataObject(applicationConfig), ctx);

        Application app = engine.getApplication(applicationId);
        app.start(ctx);

        app.createConnection("CONN-001", new DataObject(connectionConfig), ctx);
        app.connect("CONN-001", ctx);

        String token = (String) (engine.getKVStorageForApplication(applicationId).get("Token-CONN-001"));
        assertNotNull(token);

        System.out.println();
        System.out.println("Token: " + token);
        System.out.println();
    }

    @Test
    public void testCapturePic() throws Exception {
//        String endpointId = "capturePic";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Ezviz.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//        String incomingMessage = "{\"deviceSerial\":\"J57106558\", \"dataType\":0}"; // 0 URL, 1 BASE64, 2 FILE
//
//        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, incomingMessage);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        JSONObject msg = JSON.parseObject(result);
//        assertTrue( msg.containsKey("code") );
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @Test
    public void testVehicleRecognition() throws Exception {
//        String endpointId = "vehicleRecognition";
//
//        TestUtil.registerApplication()
//                .setEngine(engine)
//                .setApplicationClass(Ezviz.class.getName())
//                .setApplicationId(applicationId)
//                .setApplicationConfig(applicationConfig)
//                .setConnectionConfig(connectionConfig)
//                .setEndpointToTest(endpointId)
//                .done();
//
//        int dataType = 0; // 0 URL, 1 BASE64, 2 FILE
//        String image = null;
//
//        switch (dataType) {
//            case 0:
//                image = "https://fastdfs-gateway.ys7.com/1/capture/003h2xCsxGHVpmZioihPQoJ8ZitUNlS.jpg?" +
//                        "Expires=1669877077&OSSAccessKeyId=LTAIzI38nEHqg64n&Signature=CsUctP1x6sn2effhKqFUBlzZgqA%3D";
//                break;
//            case 1:
//                image = "/capture/J57106558/2022/11/29/185030.jpg";
//                FileStorage storage = engine.getFileStorageForApplication(applicationId);
//                try (InputStream in = storage.readFromPublicFile(image)) {
//                    byte[] bytes = IOUtils.toByteArray( in );
//                    image = Base64.getEncoder().encodeToString(bytes);
//                }
//                break;
//            case 2:
//                image = "/capture/J57106558/2022/11/29/185030.jpg";
//                break;
//        }
//
//        String incomingMessage = "{\"image\":\""+image+"\", \"dataType\":"+dataType+"}";
//
//        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, incomingMessage);
//
//        System.out.println();
//        System.out.println("Result: " + result);
//        System.out.println();
//
//        JSONObject msg = JSON.parseObject(result);
//        assertTrue( msg.containsKey("code") );
//
//        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
